package org.aron.server.core.listener.event;

import org.aron.server.core.ServletContext;
import org.aron.server.servlet.ServletRequest;

import java.util.EventObject;

/**
 * @author: Y-Aron
 * @create: 2018-12-24 23:34
 **/
public class ServletRequestEvent extends EventObject {

    private final transient ServletRequest request;

    /**
     * Constructs a prototypical Event.
     * @param source The object on which the Event initially occurred.
     * @param request request
     * @throws IllegalArgumentException if source is null.
     */
    public ServletRequestEvent(Object source, ServletRequest request) {
        super(source);
        this.request = request;
    }
    public ServletRequest getServletRequest() {
        return this.request;
    }

    public ServletContext getServletContext() {
        return (ServletContext) super.getSource();
    }
}
