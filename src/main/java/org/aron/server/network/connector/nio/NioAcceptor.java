package org.aron.server.network.connector.nio;

import org.aron.server.network.endpoint.nio.NioEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 22:12
 * Nio 请求接收器
 **/
@Slf4j
public class NioAcceptor implements Runnable{

    private NioEndpoint nioEndpoint;

    public NioAcceptor(NioEndpoint nioEndpoint) {
        this.nioEndpoint = nioEndpoint;
    }

    @Override
    public void run() {
        log.info("{} start listening ..", Thread.currentThread().getName());
        while (nioEndpoint.isRunning()) {
            SocketChannel client;
            try {
                client = nioEndpoint.accept();
                if (client == null) {
                    continue;
                }
                client.configureBlocking(false);
                log.debug("Acceptor接收到连接请求{}", client);
                nioEndpoint.registerToPoller(client);
                log.debug("socketWrapper: {}", client);
            } catch (IOException e) {
                log.error("nio error~");
//                e.printStackTrace();
            }
        }
    }
}
