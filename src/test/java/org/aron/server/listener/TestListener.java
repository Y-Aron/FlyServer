package org.aron.server.listener;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.annotation.WebListener;
import org.aron.server.core.listener.HttpSessionListener;
import org.aron.server.core.listener.ServletContextListener;
import org.aron.server.core.listener.event.HttpSessionEvent;
import org.aron.server.core.listener.event.ServletContextEvent;

/**
 * @author: Y-Aron
 * @create: 2019-02-16 14:22
 */
@Slf4j
@WebListener("init")
public class TestListener implements ServletContextListener, HttpSessionListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.error("初始化TestListener");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.error("销毁TestListener");
    }

    @Override
    public void sessionCreated(HttpSessionEvent sessionEvent) {
        log.error("初始化httpSession");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        log.error("销毁httpSession");
    }
}
