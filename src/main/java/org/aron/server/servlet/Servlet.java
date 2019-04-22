package org.aron.server.servlet;

import org.aron.server.core.ServerConfiguration;
import org.aron.server.error.ServletException;

import java.io.IOException;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 09:20
 **/
public interface Servlet {

    void init(ServerConfiguration config);

    void destroy();

    void service(ServletRequest request, ServletResponse response) throws ServletException, IOException;
}
