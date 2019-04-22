package org.aron.server.error.impl;

import lombok.extern.slf4j.Slf4j;
import org.aron.commons.utils.Utils;
import org.aron.server.error.ServletException;
import org.aron.server.error.ServletExceptionHandler;
import org.aron.server.network.wrapper.SocketWrapper;
import org.aron.server.servlet.ServletResponse;

import static org.aron.server.servlet.constant.HttpConstant.CLOSE_KEY;
import static org.aron.server.servlet.constant.HttpConstant.CLOSE_VALUE;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 17:42
 **/
@Slf4j
public class ServletExceptionHandlerImpl implements ServletExceptionHandler {

    @Override
    public void handler(ServletException e, ServletResponse response, SocketWrapper wrapper) {
        log.debug("----------开始处理Servlet异常----------");
        log.error(Utils.stackTraceToString(e));
        response.setHeader(CLOSE_KEY, CLOSE_VALUE);
        response.setStatus(e.getStatus());
        response.write(e.getErrorBody());
        log.debug("----------处理Servlet异常完毕！----------");
    }
}
