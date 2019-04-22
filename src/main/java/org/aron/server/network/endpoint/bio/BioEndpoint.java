package org.aron.server.network.endpoint.bio;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.network.connector.bio.BioAcceptor;
import org.aron.server.network.dispatcher.bio.BioDispatcher;
import org.aron.server.network.endpoint.Endpoint;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 10:45
 **/
@Slf4j
public class BioEndpoint extends Endpoint {

    private ServerSocket server;
    private BioDispatcher dispatcher;
    private volatile boolean isRunning = true;

    @Override
    public void start(int port) throws IOException {
        this.dispatcher = new BioDispatcher();
        this.server = new ServerSocket(port);
        initAcceptor();
        log.info("server is start listening to the bio mode running at http://127.0.0.1:{}", port);
    }

    @Override
    public void initAcceptor() {
        BioAcceptor acceptor = new BioAcceptor(this, dispatcher);
        Thread thread = new Thread(acceptor, "bio-acceptor");
        // 设置为守护线程
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void close() throws IOException {
        this.isRunning = false;
        this.dispatcher.shutdown();
        this.server.close();
    }

    public Socket accept() throws IOException {
        return this.server.accept();
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
