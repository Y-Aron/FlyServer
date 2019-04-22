package org.aron.server.error;

import org.aron.server.network.wrapper.SocketWrapper;
import org.aron.server.servlet.ServletResponse;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 17:41
 **/
public interface ServletExceptionHandler {

    void handler(ServletException e, ServletResponse response, SocketWrapper wrapper);
}
