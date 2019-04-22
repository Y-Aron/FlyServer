package org.aron.server.network.connector.bio;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.network.dispatcher.bio.BioDispatcher;
import org.aron.server.network.endpoint.bio.BioEndpoint;
import org.aron.server.network.wrapper.bio.BioSocketWrapper;

import java.io.IOException;
import java.net.Socket;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 10:29
 **/
@Slf4j
public class BioAcceptor implements Runnable {

    private BioEndpoint server;
    private BioDispatcher dispatcher;

    public BioAcceptor(BioEndpoint server, BioDispatcher dispatcher) {
        this.server = server;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        Socket client;
        while (server.isRunning()) {
            try {
                client = server.accept();
                log.debug("listener to the client is：{}", client);
                dispatcher.doDispatch(new BioSocketWrapper(client));
            } catch (IOException e) {
                e.printStackTrace();
                log.error("server accept client failure ..");
            }
        }
    }
}
