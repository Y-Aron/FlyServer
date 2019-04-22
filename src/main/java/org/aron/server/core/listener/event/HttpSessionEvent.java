package org.aron.server.core.listener.event;

import org.aron.server.servlet.http.HttpSession;

import java.util.EventObject;

/**
 * @author: Y-Aron
 * @create: 2019-1-1 21:59
 **/
public class HttpSessionEvent extends EventObject {

    public HttpSessionEvent(HttpSession source) {
        super(source);
    }

    public HttpSession getSession() {
        return (HttpSession) super.getSource();
    }
}