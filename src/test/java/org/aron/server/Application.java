package org.aron.server;

import org.aron.server.error.ServerException;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 12:26
 **/
public class Application {

    public static void main(String[] args) throws ServerException {
//        ServerBootStrap.build(Application.class, null, null).run();

        ServerBootStrap.build(Application.class).run();
    }

}
