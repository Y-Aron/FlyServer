package org.aron.server;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aron.context.core.impl.AnnotationApplicationContext;
import org.aron.server.core.ServerConfiguration;
import org.aron.server.error.ServerException;
import org.aron.server.network.endpoint.Endpoint;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 09:18
 **/
@Slf4j
public class ServerBootStrap {

    private static ServerBootStrap bootStrap;

    @Setter
    private ServerConfiguration configuration;

    public static ServerBootStrap build(Class<?> root) throws ServerException {
        return build(root, null, null);
    }

    public static ServerBootStrap build(Class<?> root, String[] scanPkg, String[] filterPkg) throws ServerException {
        return build(root, scanPkg, filterPkg, null);
    }

    public static ServerBootStrap build(Class<?> root, String[] scanPkg) throws ServerException {
        return build(root, scanPkg, null);
    }

    public static ServerBootStrap build(Class<?>... classes) throws ServerException {
        return build(null, null, null, classes);
    }

    public static ServerBootStrap build(AnnotationApplicationContext context) throws ServerException {
        bootStrap = new ServerBootStrap();
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.init(context);
        bootStrap.setConfiguration(configuration);
        return bootStrap;
    }

    private static ServerBootStrap build(Class<?> root, String[] scanPkg, String[] filterPkg, Class<?>[] classes) throws ServerException {
        bootStrap = new ServerBootStrap();
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.init(root, scanPkg, filterPkg, classes);
        bootStrap.setConfiguration(configuration);
        return bootStrap;
    }

    public void run() {
        Endpoint endpoint = null;
        try {
            endpoint = Endpoint.getInstance(ServerConfiguration.CONNECTOR);
            endpoint.startEndpoint(ServerConfiguration.PORT);
            Scanner scanner = new Scanner(System.in);
            String order;
            while (scanner.hasNext()) {
                order = scanner.next();
                if ("exit".equalsIgnoreCase(order)) {
                    endpoint.close();
                    System.exit(0);
                }
            }
        } catch (ServerException | IOException e) {
            e.printStackTrace();
            log.error("Server startup failed ...");
            assert endpoint != null;
            try {
                endpoint.close();
            } catch (ServerException | IOException e1) {
                e1.printStackTrace();
            }
            System.exit(0);
        }
    }
}
