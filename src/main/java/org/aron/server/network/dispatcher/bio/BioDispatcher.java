package org.aron.server.network.dispatcher.bio;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.network.dispatcher.AbstractDispatcher;
import org.aron.server.network.handler.bio.BioRequestHandler;
import org.aron.server.network.wrapper.SocketWrapper;
import org.aron.server.network.wrapper.bio.BioSocketWrapper;
import org.aron.server.servlet.http.HttpServletRequest;
import org.aron.server.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 10:58
 **/
@Slf4j
public class BioDispatcher extends AbstractDispatcher {

    @Override
    public void doDispatch(SocketWrapper socketWrapper) {
        BioSocketWrapper wrapper = (BioSocketWrapper) socketWrapper;
        HttpServletRequest request;
        HttpServletResponse response = new HttpServletResponse();
        try {
            request = new HttpServletRequest(wrapper.getSocket());
            pool.execute(new BioRequestHandler(wrapper, servletContext, handler, request, response));
        }  catch (IOException e) {
            e.printStackTrace();
            this.closeSocketWrapper(wrapper);
        }
    }
}
