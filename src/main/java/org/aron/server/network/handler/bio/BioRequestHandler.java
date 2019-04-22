package org.aron.server.network.handler.bio;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.core.ServletContext;
import org.aron.server.error.ServletExceptionHandler;
import org.aron.server.network.handler.AbstractRequestHandler;
import org.aron.server.network.wrapper.SocketWrapper;
import org.aron.server.network.wrapper.bio.BioSocketWrapper;
import org.aron.server.servlet.ServletRequest;
import org.aron.server.servlet.ServletResponse;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 12:24
 **/
@Slf4j
public class BioRequestHandler extends AbstractRequestHandler {

    public BioRequestHandler(SocketWrapper socketWrapper, ServletContext servletContext,
                             ServletExceptionHandler handler, ServletRequest servletRequest, ServletResponse servletResponse) {
        super(socketWrapper, servletContext, handler, servletRequest, servletResponse);
    }

    @Override
    public void flushResponse() throws IOException {
        log.debug("---------- bio servletResponse ----------");
        BioSocketWrapper socketWrapper = (BioSocketWrapper) this.socketWrapper;
        OutputStream os = socketWrapper.getSocket().getOutputStream();
        byte[] resp = servletResponse.getResponseBytes();
        log.debug("响应报文：{}", new String(resp));
        os.write(resp);
        os.flush();
        os.close();
        socketWrapper.close();
    }
}
