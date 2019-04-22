package org.aron.server.servlet.http.enumeration;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 12:35
 **/
public enum HttpStatus {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    BAD_REQUEST(400, "Bad ServletRequest"),
    FORBIDDEN(403, "Forbidden");

    private int code;
    private String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
