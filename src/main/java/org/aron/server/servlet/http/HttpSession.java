package org.aron.server.servlet.http;

import lombok.Getter;
import lombok.Setter;
import org.aron.server.servlet.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Y-Aron
 * @create: 2019-01-01 21:48
 **/
public class HttpSession implements Session {

    @Getter
    private String id;
    private Map<String, Object> attributes;
    private boolean isValid;

    private long maxInactiveInterval = -1;

    @Getter
    private long lastAccessedTime;

    private HttpServletResponse response;

    @Setter
    @Getter
    private long creationTime;

    public HttpSession(String id, final HttpServletResponse response) {
        this.id = id;
        this.response = response;
        this.attributes = new ConcurrentHashMap<>(0);
        this.isValid = true;
        this.lastAccessedTime = System.currentTimeMillis();
    }

    public void setMaxInactiveInterval(long maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    /**
     * 使当前session失效 之后就无法读写当前session
     * 清楚session数据 并且在servletContext中删除此session
     */
    public void invalidate() {
        this.isValid = false;
        this.attributes.clear();
    }

    public Object getAttribute(String key) {
        if (this.isValid) {
            this.lastAccessedTime = System.currentTimeMillis();
            return attributes.get(key);
        }
        throw new IllegalStateException("http session has invalidated");
    }

    public void setAttribute(String key, Object value) {
        if (this.isValid) {
            this.attributes.put(key, value);
            this.lastAccessedTime = System.currentTimeMillis();
        } else {
            throw new IllegalStateException("http session has invalidated");
        }
    }

    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }
}
