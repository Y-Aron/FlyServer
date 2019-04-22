package org.aron.server.network.handler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aron.server.core.ServletContext;
import org.aron.server.error.ServletException;
import org.aron.server.error.ServletExceptionHandler;
import org.aron.server.network.wrapper.SocketWrapper;
import org.aron.server.servlet.*;

import java.io.IOException;
import java.util.List;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 11:28
 **/
@Slf4j
@Getter
public abstract class AbstractRequestHandler implements Runnable, FilterChain {

    protected ServletRequest servletRequest;
    protected ServletResponse servletResponse;
    protected SocketWrapper socketWrapper;
    protected boolean isFinished;
    protected ServletContext servletContext;

    protected ServletExceptionHandler handler;

    protected Servlet servlet;
    protected List<Filter> filters;
    private int filterIndex = 0;
    private Filter currentFilter;

    public AbstractRequestHandler(SocketWrapper socketWrapper, ServletContext servletContext,
                                  ServletExceptionHandler handler, ServletRequest servletRequest, ServletResponse servletResponse) {
        this.servletContext = servletContext;
        this.socketWrapper = socketWrapper;
        this.handler = handler;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.isFinished = false;
        servletRequest.setRequestHandler(this);
        servletRequest.setServletContext(servletContext);
    }

    @Override
    public void run() {
        try {
            String mimeType = servletRequest.getMimeType();
            String url = servletRequest.getUrl();
            if (StringUtils.isNotBlank(mimeType)) {
                servletResponse.writeFile(url, mimeType);
                destroy();
                return;
            }
            if (!StringUtils.contains(url, ".")) {
                // 根据url查询匹配的servlet 结果是0个或一个
                servlet = servletContext.mapServlet(url);
                // 根据url查询匹配的filter 结果是1个或多个
                filters = servletContext.mapFilter(url);
            }
            log.debug("servlet: {}", servlet);
            log.debug("filters: {}", filters);
            if (CollectionUtils.isEmpty(filters)) {
                service();
            } else {
                doFilter(servletRequest, servletResponse);
            }
        } catch (ServletException e) {
            handler.handler(e, servletResponse, socketWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            destroy();
        }
    }

    /**
     * 执行过滤器链
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        // 执行拦截器的销毁方法
        if (filterIndex < filters.size()) {
            currentFilter = filters.get(filterIndex ++);
            // 执行拦截器
            currentFilter.doFilter(request, response, this);
        } else {
            service();
        }
    }

    private void service() throws ServletException, IOException {
        servlet.init(servletContext.getServerConfiguration());
        servlet.service(servletRequest, servletResponse);
        servlet.destroy();
    }

    private void destroy() {
        if (!isFinished) {
            this.isFinished = true;
            try {
                flushResponse();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void flushResponse() throws IOException;
}
