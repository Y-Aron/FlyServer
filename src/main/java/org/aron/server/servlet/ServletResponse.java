package org.aron.server.servlet;

import org.aron.server.error.ServletException;
import org.aron.server.servlet.http.enumeration.HttpStatus;

import java.nio.ByteBuffer;

public interface ServletResponse {

    void setHeader(String key, String value);

    String getHeader(String key);

    void setContentType(String contentType);

    String getContentType();

    void setStatus(HttpStatus status);

    // 获取响应数据流
    byte[] getResponseBytes();

    ByteBuffer[] getResponseBuffers();

    void write(byte[] bytes);

    void write(String str);

    void write(Object object);

    void close();

    void writeFile(String uri, String mimeType) throws ServletException;

    void writeFile(String filename, byte[] bytes) throws ServletException;

    void setCharacterEncoding(String encoding);

}
