package org.aron.server.servlet;

/**
 * @author: Y-Aron
 * @create: 2019-02-11 21:35
 */
public interface SessionContext {
    /**
     *销毁session
     */
    void invalidateSession(Session session);

    /**
     * 创建session
     */
    Session createSession(ServletResponse response);

    /**
     * 获取session
     * @param JSESSIONID 保存在cookie中的session唯一标识
     * @return 返回session管理器
     */
    Session getSession(String JSESSIONID);
}
