package org.aron.server.servlet.http.enumeration;

import lombok.Getter;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 13:48
 **/
public enum HttpRequestMethod {
    GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE"), PATCH("PATCH"), OPTIONS("OPTIONS");

    @Getter
    private String name;

    HttpRequestMethod() {}

    HttpRequestMethod(String name) {
        this.name = name;
    }
}
