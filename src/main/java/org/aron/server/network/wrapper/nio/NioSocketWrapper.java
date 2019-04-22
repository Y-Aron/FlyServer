package org.aron.server.network.wrapper.nio;

import org.aron.server.network.connector.nio.NioPoller;
import org.aron.server.network.endpoint.nio.NioEndpoint;
import org.aron.server.network.wrapper.SocketWrapper;
import lombok.Data;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 20:52
 **/
@Data
public class NioSocketWrapper implements SocketWrapper {

    private final NioEndpoint server;
    private final SocketChannel socketChannel;
    private final NioPoller nioPoller;
    private final boolean isNewSocket;
    private volatile long waitBegin;
    private volatile boolean isWorking;

    public NioSocketWrapper(NioEndpoint server, SocketChannel socketChannel, NioPoller nioPoller, boolean isNewSocket) {
        this.server = server;
        this.socketChannel = socketChannel;
        this.nioPoller = nioPoller;
        this.isNewSocket = isNewSocket;
        this.isWorking = false;
    }

    /**
     * 关闭服务器
     */
    @Override
    public void close() throws IOException {
        socketChannel.keyFor(nioPoller.getSelector()).cancel();
        socketChannel.close();
    }

    @Override
    public String toString() {
        return socketChannel.toString();
    }
}
