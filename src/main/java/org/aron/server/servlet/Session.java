package org.aron.server.servlet;

/**
 * @author: Y-Aron
 * @create: 2019-02-11 21:14
 */
public interface Session {

    void invalidate();

    Object getAttribute(String key);

    void setAttribute(String key, Object value);

    void removeAttribute(String key);
}
