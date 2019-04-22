package org.aron.server.network.handler.nio;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.core.ServletContext;
import org.aron.server.error.ServletExceptionHandler;
import org.aron.server.network.handler.AbstractRequestHandler;
import org.aron.server.network.wrapper.SocketWrapper;
import org.aron.server.network.wrapper.nio.NioSocketWrapper;
import org.aron.server.servlet.ServletRequest;
import org.aron.server.servlet.ServletResponse;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.aron.server.servlet.constant.HttpConstant.CLOSE_KEY;
import static org.aron.server.servlet.constant.HttpConstant.CLOSE_VALUE;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 22:52
 **/
@Slf4j
public class NioRequestHandler extends AbstractRequestHandler {

    public NioRequestHandler(SocketWrapper socketWrapper, ServletContext servletContext,
                             ServletExceptionHandler handler, ServletRequest servletRequest, ServletResponse servletResponse) {
        super(socketWrapper, servletContext, handler, servletRequest, servletResponse);
    }

    /**
     * HTTP/1.1 200 OK
     * Date: Mon Feb 04 21:37:17 CST 2019
     * Content-Type: text/html;charset=utf-8
     * Set-Cookie: c1=v1; path=/test; max-age=10
     * Content-Length: 4
     */
    @Override
    public void flushResponse() throws IOException {
        log.debug("----------nio servletResponse ----------");
        NioSocketWrapper wrapper = (NioSocketWrapper) this.socketWrapper;
        ByteBuffer[] buffers = servletResponse.getResponseBuffers();
        wrapper.getSocketChannel().write(buffers);

        String connection = servletResponse.getHeader(CLOSE_KEY);
        log.debug("connection: {}", connection);
        if (CLOSE_VALUE.equals(connection)) {
            log.debug("CLOSE: 客户端连接{} 已关闭", wrapper.getSocketChannel());
            wrapper.close();
        } else {
            // 异常或长连接则重新注册
            log.debug("KEEP-ALIVE：客户端连接{} 重新注册到Poller中", wrapper.getSocketChannel());
            wrapper.getNioPoller().register(wrapper.getSocketChannel(), false);
        }
    }
}
