package org.aron.server.servlet.http;

import lombok.extern.slf4j.Slf4j;
import org.aron.commons.utils.Utils;
import org.aron.server.core.ServerConfiguration;
import org.aron.server.core.ServletContext;
import org.aron.server.error.ServletException;
import org.aron.server.servlet.Servlet;
import org.aron.server.servlet.ServletRequest;
import org.aron.server.servlet.ServletResponse;
import org.aron.server.servlet.http.enumeration.HttpRequestMethod;
import org.aron.server.servlet.http.enumeration.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.aron.server.servlet.http.enumeration.HttpRequestMethod.*;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 13:35
 **/
@Slf4j
public abstract class HttpServlet implements Servlet {

    private ServletContext servletContext;

    @Override
    public void init(ServerConfiguration config) { }

    @Override
    public void destroy() { }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        service(req, resp);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Method[] methods = Utils.getDeclaredOverrideMethods(this.getClass(), HttpServlet.class);
        HttpRequestMethod httpRequestMethod = request.getMethod();
        boolean accept = false;
        for (Method method : methods) {
            String name = method.getName();
            if ("service".equals(name)) {
                return;
            } else if ("doGet".equals(name) && httpRequestMethod.equals(GET)) {
                doGet(request, response);
                accept = true;
            } else if ("doPost".equals(name) && httpRequestMethod.equals(POST)) {
                doPost(request, response);
                accept = true;
            } else if ("doPut".equals(name) && httpRequestMethod.equals(PUT)) {
                doPut(request, response);
                accept = true;
            } else if ("doDelete".equals(name) && httpRequestMethod.equals(DELETE)) {
                doDelete(request, response);
                accept = true;
            } else if ("doPatch".equals(name) && httpRequestMethod.equals(PATCH)) {
                doPatch(request, response);
                accept = true;
            }
        }
        if (!accept) {
            throw new ServletException(HttpStatus.NOT_FOUND, "url["+ request.getUrl() +"]: method[" + httpRequestMethod + "] is not exists ");
        }
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { }
}
