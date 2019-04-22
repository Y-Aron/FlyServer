package org.aron.server.core.listener;


import org.aron.server.core.listener.event.HttpSessionEvent;

import java.util.EventListener;

/**
 * @author: Y-Aron
 * @create: 2019-1-1 21:59
 **/
public interface HttpSessionListener extends EventListener {
    /**
     * session创建
     * @param sessionEvent
     */
    void sessionCreated(HttpSessionEvent sessionEvent);

    /**
     * session销毁
     * @param sessionEvent
     */
    void sessionDestroyed(HttpSessionEvent sessionEvent);
    
}
