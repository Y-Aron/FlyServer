package org.aron.server.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aron.commons.utils.AntPathMatcher;
import org.aron.commons.utils.Utils;
import org.aron.context.core.impl.AnnotationApplicationContext;
import org.aron.context.error.AnnotationException;
import org.aron.context.error.BeanInstantiationException;
import org.aron.server.annotation.WebFilter;
import org.aron.server.annotation.WebListener;
import org.aron.server.annotation.WebServlet;
import org.aron.server.core.holder.FilterHolder;
import org.aron.server.core.holder.ServletHolder;
import org.aron.server.core.listener.HttpSessionListener;
import org.aron.server.core.listener.ServletContextListener;
import org.aron.server.core.listener.event.HttpSessionEvent;
import org.aron.server.core.listener.event.ServletContextEvent;
import org.aron.server.error.ServerException;
import org.aron.server.error.ServletException;
import org.aron.server.servlet.*;
import org.aron.server.servlet.http.HttpServlet;
import org.aron.server.servlet.http.HttpSession;
import org.aron.server.servlet.http.HttpSessionContext;
import org.aron.server.servlet.http.enumeration.HttpStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 13:35
 **/
@Slf4j
public class ServletContext implements SessionContext {

    private AntPathMatcher matcher = new AntPathMatcher();

    private Map<String, ServletHolder> servletHolderMap;

    private Map<String, String> servletMapping;

    private Map<String, FilterHolder> filterHolderMap;

    private Map<String, List<String>> filterMapping;

    private List<HttpSessionListener> httpSessionListeners;

    private List<ServletContextListener> servletContextListeners;

    private HttpSessionContext sessionContext;

    private AnnotationApplicationContext aapContext;

    @Setter
    @Getter
    private ServerConfiguration serverConfiguration;

    public ServletContext() {
        this.servletHolderMap = new ConcurrentHashMap<>(0);
        this.servletMapping = new ConcurrentHashMap<>(0);
        this.filterHolderMap = new ConcurrentHashMap<>(0);
        this.filterMapping = new ConcurrentHashMap<>(0);
        this.sessionContext = HttpSessionContext.getInstance();
        this.httpSessionListeners = new ArrayList<>(0);
        this.servletContextListeners = new ArrayList<>(0);
    }

    public void destroy() {
        // 销毁Servlet
        this.servletHolderMap.values().forEach(servletHolder -> {
            if (servletHolder.getServlet() != null) {
                servletHolder.getServlet().destroy();
            }
        });
        // 销毁Filter
        this.filterHolderMap.values().forEach(filterHolder -> {
            if (filterHolder.getFilter() != null) {
                log.debug("----------执行Filter【"+ filterHolder.getFilter() +"】 destroy()方法----------");
                filterHolder.getFilter().destroy();
            }
        });
        // 注销Event
        ServletContextEvent event = new ServletContextEvent(this);
        for (ServletContextListener listener: servletContextListeners){
            listener.contextDestroyed(event);
        }
    }

    private Servlet initAndGetServlet(String servletName) throws ServletException {
        ServletHolder holder = this.servletHolderMap.get(servletName);
        if (holder == null) {
            throw new ServletException(HttpStatus.NOT_FOUND);
        }
        if (holder.getServlet() == null) {
            try {
                Servlet servlet = (Servlet) holder.getServletClass().newInstance();
                holder.setServlet(servlet);
            } catch (Exception e) {
                throw new ServletException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return holder.getServlet();
    }

    public Servlet mapServlet(String url) throws ServletException {
        // 1.精确匹配
        String servletName = this.servletMapping.get(url);
        if (servletName != null) {
            return initAndGetServlet(servletName);
        }
        // 2.路径匹配
        List<String> matchingPatterns = new ArrayList<>();
        Set<String> patterns = this.servletMapping.keySet();
        for (String pattern : patterns) {
            if (matcher.match(pattern, url)) {
                matchingPatterns.add(pattern);
            }
        }
        if (!matchingPatterns.isEmpty()) {
            Comparator<String> comparator = matcher.getPatternComparator(url);
            matchingPatterns.sort(comparator);
            String bestMatch = matchingPatterns.get(0);
            return initAndGetServlet(this.servletMapping.get(bestMatch));
        }
        throw new ServletException(HttpStatus.NOT_FOUND);
    }

    public List<Filter> mapFilter(String url) throws ServletException {
        List<String> matchingPatterns = new ArrayList<>();
        Set<String> patterns = this.filterMapping.keySet();
        for (String pattern : patterns) {
            if (matcher.match(pattern, url)) {
                matchingPatterns.add(pattern);
            }
        }
        Set<String> filterAliases = matchingPatterns.stream().flatMap(pattern ->
                this.filterMapping.get(pattern).stream()).collect(Collectors.toSet());
        List<Filter> result = new ArrayList<>();
        for (String alias : filterAliases) {
            FilterHolder filterHolder = this.filterHolderMap.get(alias);
            if (filterHolder == null) {
                throw new ServletException(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                result.add(filterHolder.getFilter());
            }
        }
        return result;
    }

    /**
     * 启动服务器的时候初始化过滤器的 init() 方法
     */
    public void initFilter() throws ServerException {
        for (Map.Entry<String, FilterHolder> entry : this.filterHolderMap.entrySet()) {
            FilterHolder holder = entry.getValue();
            Filter filter = holder.getFilter();
            if (filter == null) {
                try {
                    filter = (Filter) entry.getValue().getFilterClass().newInstance();
                } catch (Exception e) {
                    throw new ServerException(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            holder.setFilter(filter);
            log.debug("----------执行过滤器【" + entry.getKey() + "】的 init()方法----------");
            try {
                filter.init(this);
            } catch (ServletException e) {
                throw new ServerException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * 创建上下文
     * 1. 获取含有@WebServlet、@WebFilter注解的类
     * 2. 初始化servletHolderMap、servletMapping、filterHolderMap、filterMapping
     */
    public void buildContext(AnnotationApplicationContext context) throws ServerException, ClassNotFoundException {
        this.aapContext = context;
        try {
            build(context.getClassWithAnnotation(WebServlet.class),
                    context.getClassWithAnnotation(WebFilter.class), context.getClassWithAnnotation(WebListener.class));
        } catch (AnnotationException | BeanInstantiationException e) {
            throw new ClassNotFoundException(e.getMessage());
        }
    }

    private void build(Set<Class<?>> webServlets, Set<Class<?>> webFilters, Set<Class<?>> webListeners) throws ServerException, AnnotationException, BeanInstantiationException {
        log.debug("----------开始构建servlet----------");
        for (Class<?> webServlet : webServlets) {
            log.debug("web servlet: {}", webServlet);
            setWebServlet(webServlet);
        }
        log.debug("----------构建servlet完毕！----------");
        log.debug("----------开始构建filter----------");
        for (Class<?> webFilter : webFilters) {
            log.debug("web filter: {}", webFilter);
            setWebFilter(webFilter);
        }
        log.debug("----------构建filter完毕！----------");

        log.debug("----------开始构建listener----------");
        for (Class<?> webListener : webListeners) {
            log.debug("web listener: {}", webListener);
            setWebListener(webListener);
        }
        log.debug("----------构建listener完毕！----------");
//        show();
    }

    private void setWebListener(Class<?> webListener) throws ServerException {
        try {
            EventListener listener = (EventListener) webListener.newInstance();
            if (listener instanceof ServletContextListener) {
                servletContextListeners.add((ServletContextListener) listener);
            }
            if (listener instanceof HttpSessionListener) {
                httpSessionListeners.add((HttpSessionListener) listener);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServerException(webListener + " instance error ...");
        }
    }

    private void show() {
        this.servletHolderMap.forEach((k, v) -> {
            log.debug("servlet name: {}, holder: {}", k, v);
        });

        this.servletMapping.forEach((k, v) -> {
            log.debug("url pattern: {}, servlet name: {}", k, v);
        });

        this.filterHolderMap.forEach((k, v) -> {
            log.debug("filter name: {}, holder: {}", k, v);
        });

        this.filterMapping.forEach((k, v) -> {
            log.debug("url pattern: {}, filter name: {}", k, v);
        });
    }

    /**
     * 初始化servletHolderMap、servletMapping
     * 1. 校验clazz是否是HttpServlet的子类并获取servletName
     * 2. servletHolderMap: servletName -> ServletHolder 一对一
     * 3. servletMapping: urlPattern -> servletName 一对一
     * 4. 一个urlPattern对应一个servletName 一个servletName对应一个servlet
     */
    public void setWebServlet(Class<?> clazz) throws ServerException, AnnotationException, BeanInstantiationException {
        WebServlet webServlet = clazz.getAnnotation(WebServlet.class);
        String servletName = this.verifyAndGetName(clazz, HttpServlet.class, webServlet.name());
        if (this.servletHolderMap.containsKey(servletName)) {
            throw new ServerException(clazz + " is exists");
        }
        // 注入servlet
        ServletHolder holder = new ServletHolder(clazz);
        if (this.aapContext != null) {
            Servlet servlet = (Servlet) this.aapContext.getBean(clazz);
            if (servlet == null) {
                servlet = (Servlet) this.aapContext.setBean(clazz);
            }
            holder.setServlet(servlet);
        }
        this.servletHolderMap.put(servletName, holder);
        // 注入servlet-mapping
        String[] urlPatterns = webServlet.value();
        Set<String> keys = this.servletMapping.keySet();
        for (String urlPattern : urlPatterns) {
            String url = verifyAndGetUrlPattern(urlPattern, keys, false);
            this.servletMapping.put(url, servletName);
        }
    }

    /**
     * 初始化 filterHolderMap、filterMapping
     * 1. 校验clazz是否是HttpServlet的子类并获取 filterName
     * 2. filterHolderMap: filterName -> FilterHolder 一对一
     * 3. filterMapping: urlPattern -> filterName 一对多
     * 4. 一个urlPattern对应一个至多个filter 一个filterName对应多个Filter
     */
    public void setWebFilter(Class<?> clazz) throws ServerException, AnnotationException, BeanInstantiationException {
        WebFilter webFilter = clazz.getAnnotation(WebFilter.class);
        String filterName = this.verifyAndGetName(clazz, Filter.class, webFilter.name());
        if (this.filterHolderMap.containsKey(filterName)) {
            throw new ServerException(clazz + " is exists");
        }
        FilterHolder holder = new FilterHolder(clazz);
        if (this.aapContext != null) {
            Filter filter = (Filter) this.aapContext.setBean(clazz);
            holder.setFilter(filter);
        }
        this.filterHolderMap.put(filterName, holder);
        String[] urlPatterns = webFilter.value();
        Set<String> keys = this.filterMapping.keySet();
        for (String urlPattern : urlPatterns) {
            String url = verifyAndGetUrlPattern(urlPattern, keys, true);
            List<String> values = filterMapping.computeIfAbsent(url, k -> new ArrayList<>(0));
            values.add(filterName);
        }
    }

    /**
     * 校验URL是否符合标准
     */
    private String verifyAndGetUrlPattern(String urlPattern, Set<String> keys, boolean more) throws ServerException {
        if (!Utils.matchUrl(urlPattern, false)) {
            throw new ServerException("url[" + urlPattern + "] is not standard");
        }
        if (!more && keys.contains(urlPattern)) {
            throw new ServerException("url[" + urlPattern + "] is exists");
        }
        return urlPattern;
    }

    /**
     * 校验name是否符合标准
     */
    private String verifyAndGetName(Class<?> clazz, Class<?> parent, String name) throws ServerException {
        if (!parent.isAssignableFrom(clazz)) {
            throw new ServerException(clazz + " is not a ["+ parent +"] or a subclass of it");
        }
        if (StringUtils.isBlank(name)) {
            name = clazz.getName();
        }
        return name;
    }

    @Override
    public void invalidateSession(Session session) {
        sessionContext.invalidateSession(session);
        afterSessionDestroyed(session);
    }

    @Override
    public Session createSession(ServletResponse response) {
        HttpSession session = sessionContext.createSession(response);
        HttpSessionEvent event = new HttpSessionEvent(session);
        for (HttpSessionListener listener : httpSessionListeners) {
            listener.sessionCreated(event);
        }
        return session;
    }

    @Override
    public Session getSession(String JSESSIONID) {
        return sessionContext.getSession(JSESSIONID);
    }

    private void afterSessionDestroyed(Session session) {
        HttpSessionEvent httpSessionEvent = new HttpSessionEvent((HttpSession) session);
        for (HttpSessionListener listener : httpSessionListeners) {
            listener.sessionDestroyed(httpSessionEvent);
        }
    }
}
