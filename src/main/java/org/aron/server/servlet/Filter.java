package org.aron.server.servlet;

import org.aron.server.core.ServletContext;
import org.aron.server.error.ServletException;

import java.io.IOException;

/**
 * @author: Y-Aron
 * @create: 2018-12-25 11:27
 **/
public interface Filter {

    void init(ServletContext context) throws ServletException;

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException;

    void destroy();
}
