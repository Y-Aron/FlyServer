package org.aron.server.error;

import org.aron.server.servlet.http.enumeration.HttpStatus;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 12:36
 **/
public class ServerException extends Exception{

    private String message = "server startup error";

    public ServerException(HttpStatus notFound) {}

    public ServerException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
