package org.aron.server.network.endpoint;


import org.apache.commons.lang3.StringUtils;
import org.aron.server.core.ServerConfiguration;
import org.aron.server.core.ServletContext;
import org.aron.server.error.ServerException;

import java.io.IOException;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 10:30
 **/
public abstract class Endpoint {

    public abstract void initAcceptor();

    public abstract void start(int port) throws IOException;

    /**
     * 启动服务器的同时启动过滤器
     * @param port 端口
     */
    public void startEndpoint(int port) throws IOException, ServerException {
        ServletContext context = ServerConfiguration.getServletContext();
        context.initFilter();
        start(port);
    }

    public abstract void close() throws ServerException, IOException;

    public static Endpoint getInstance(String connector) throws ServerException {
        connector = StringUtils.lowerCase(connector);
        StringBuilder sb = new StringBuilder();
        sb.append(Endpoint.class.getPackage().getName())
                .append(".")
                .append(connector)
                .append(".")
                .append(StringUtils.capitalize(connector))
                .append("Endpoint");
        try {
            return (Endpoint) Class.forName(sb.toString()).newInstance();
        } catch (Exception e) {
            throw new ServerException(e.getMessage());
        }
    }
}
