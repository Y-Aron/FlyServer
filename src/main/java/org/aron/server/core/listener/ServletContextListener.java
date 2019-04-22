package org.aron.server.core.listener;

import org.aron.server.core.listener.event.ServletContextEvent;

import java.util.EventListener;

/**
 * @author: Y-Aron
 * @create: 2018-12-24 23:34
 **/
public interface ServletContextListener extends EventListener {

    /**
     * 请求初始化
     * @param event
     */
    void contextInitialized(ServletContextEvent event);

    /**
     * 请求销毁
     * @param event
     */
    void contextDestroyed(ServletContextEvent event);
}
