package org.aron.server.error;

import lombok.Getter;
import org.aron.server.servlet.http.enumeration.HttpStatus;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 13:31
 **/
public class ServletException extends Exception {

    private static final long serialVersionUID = -1230546147346401277L;

    private String message = "";

    @Getter
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public ServletException() {}

    public ServletException(String message) {
        this.message = message;
    }

    public ServletException(HttpStatus status) {
        this.status = status;
    }

    public ServletException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return status.getCode() + " / " + status.getMessage();
    }

    public String getErrorBody() {
        return status.getCode() + " / " + status.getMessage();
    }
}
