package org.aron.server.servlet;

import org.aron.server.error.ServletException;

import java.io.IOException;

/**
 * @author: Y-Aron
 * @create: 2019-01-01 10:13
 **/
public interface FilterChain {

    void doFilter(ServletRequest request, ServletResponse response) throws ServletException, IOException;

}
