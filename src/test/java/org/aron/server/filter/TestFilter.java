package org.aron.server.filter;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.annotation.WebFilter;
import org.aron.server.core.ServletContext;
import org.aron.server.error.ServletException;
import org.aron.server.servlet.Filter;
import org.aron.server.servlet.FilterChain;
import org.aron.server.servlet.ServletRequest;
import org.aron.server.servlet.ServletResponse;

import java.io.IOException;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 12:28
 **/
@WebFilter({"/**", "/aac"})
@Slf4j
public class TestFilter implements Filter {

    @Override
    public void init(ServletContext context) throws ServletException {
        log.debug("TestFilter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        log.debug("TestFilter doFilter");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.debug("TestFilter destroy");
    }
}
