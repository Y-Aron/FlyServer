package org.aron.server.network.dispatcher.nio;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.network.dispatcher.AbstractDispatcher;
import org.aron.server.network.handler.nio.NioRequestHandler;
import org.aron.server.network.wrapper.SocketWrapper;
import org.aron.server.network.wrapper.nio.NioSocketWrapper;
import org.aron.server.servlet.http.HttpServletRequest;
import org.aron.server.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 22:28
 **/
@Slf4j
public class NioDispatcher extends AbstractDispatcher {

    @Override
    public void doDispatch(SocketWrapper socketWrapper) {
        NioSocketWrapper wrapper = (NioSocketWrapper) socketWrapper;
        log.debug("----------已经将请求放入worker线程池中----------");
        log.debug("----------开始读取HttpRequest----------");
        HttpServletResponse response = new HttpServletResponse();
        try {
            SocketChannel channel = wrapper.getSocketChannel();
            HttpServletRequest request = new HttpServletRequest(channel);
            pool.execute(new NioRequestHandler(wrapper, servletContext, handler, request, response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
