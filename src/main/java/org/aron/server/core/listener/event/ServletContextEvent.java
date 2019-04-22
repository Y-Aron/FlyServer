package org.aron.server.core.listener.event;

import org.aron.server.core.ServletContext;

import java.util.EventObject;

/**
 * @author: Y-Aron
 * @create: 2018-12-24 23:34
 **/
public class ServletContextEvent extends EventObject {

    /**
     * Constructs a prototypical Event.
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ServletContextEvent(Object source) {
        super(source);
    }

    public ServletContext getServletContext() {
        return (ServletContext) super.getSource();
    }
}
