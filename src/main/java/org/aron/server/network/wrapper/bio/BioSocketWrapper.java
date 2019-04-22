package org.aron.server.network.wrapper.bio;

import lombok.Getter;
import org.aron.server.network.wrapper.SocketWrapper;

import java.io.IOException;
import java.net.Socket;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 09:28
 **/
@Getter
public class BioSocketWrapper implements SocketWrapper {

    private Socket socket;

    public BioSocketWrapper(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    @Override
    public String toString() {
        return socket.toString();
    }
}
