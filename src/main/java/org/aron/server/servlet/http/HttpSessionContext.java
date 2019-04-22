package org.aron.server.servlet.http;

import org.aron.commons.utils.Utils;
import org.aron.server.servlet.ServletResponse;
import org.aron.server.servlet.Session;
import org.aron.server.servlet.SessionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.aron.server.core.ServerConfiguration.SESSION_NAME;

/**
 * @author: Y-Aron
 * @create: 2019-02-11 21:11
 */
public class HttpSessionContext implements SessionContext {

    private Map<String, HttpSession> sessionMap;

    public static HttpSessionContext getInstance() {
        return Singleton.INSTANCE.getSingleton();
    }

    private enum Singleton {
        INSTANCE;
        private HttpSessionContext singleton;
        Singleton() {
            singleton = new HttpSessionContext();
            singleton.sessionMap = new ConcurrentHashMap<>(1);
        }
        public HttpSessionContext getSingleton() {
            return singleton;
        }
    }

    @Override
    public void invalidateSession(Session session) {
        HttpSession httpSession = (HttpSession) session;
        sessionMap.remove(httpSession.getId());
    }

    @Override
    public HttpSession createSession(ServletResponse response) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpSession session = new HttpSession(Utils.generateUUID(), httpServletResponse);
        session.setCreationTime(System.currentTimeMillis());
        sessionMap.put(session.getId(), session);
        Cookie cookie = new Cookie(SESSION_NAME, session.getId());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        httpServletResponse.addCookie(cookie);
        return session;
    }

    @Override
    public HttpSession getSession(String JSESSIONID) {
        return sessionMap.get(JSESSIONID);
    }
}
