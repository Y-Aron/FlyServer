package org.aron.server.servlet;

import lombok.extern.slf4j.Slf4j;
import org.aron.commons.date.DateUtils;
import org.aron.server.annotation.WebServlet;
import org.aron.server.core.ServerConfiguration;
import org.aron.server.error.ServletException;
import org.aron.server.servlet.http.HttpServlet;
import org.aron.server.servlet.http.HttpServletRequest;
import org.aron.server.servlet.http.HttpServletResponse;
import org.aron.server.servlet.http.HttpSession;

import java.io.IOException;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 12:27
 **/
@Slf4j
@WebServlet("/**")
public class TestServlet extends HttpServlet {

    @Override
    public void init(ServerConfiguration config) {
        log.debug("servlet init");
    }

    @Override
    public void destroy() {
        log.debug("servlet destroy");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("----------测试request----------");
        HttpSession session = request.getSession();
        log.debug("create: {}", DateUtils.convertTime2Str(session.getCreationTime()));
        log.debug("last: {}", DateUtils.convertTime2Str(session.getLastAccessedTime()));

        Object attribute = session.getAttribute("name");
        log.debug("attr: {}", attribute);

        session.setAttribute("name", 1234);
        log.debug("{}", session.getAttribute("name"));

        log.debug("last: {}", DateUtils.convertTime2Str(session.getLastAccessedTime()));
        log.debug("last: {}", DateUtils.convertTime2Str(session.getLastAccessedTime()));
        log.debug("last: {}", DateUtils.convertTime2Str(session.getLastAccessedTime()));

//        log.info("url parameter: {}", request.getParameter("username"));
//        log.info("url parameter: {}", request.getParameter("passwd"));
//        log.info("url parameter: {}", request.getParameter("asd"));
//        log.info("cookie: {}", request.getCookie("cookie1"));
//        log.info("header: {}", request.getHeader("Accept"));
//        log.info("file: {}", request.getFile("file"));
//        log.info("files: {}", (Object) request.getFiles("f1"));
//        log.info("----------测试response----------");
//        log.info("----------测试response cookie----------");
//        Cookie cookie = new Cookie("c1", "v1");
//        cookie.setHttpOnly(true);
//        cookie.setPath("/test");
//        cookie.setMaxAge(10);
//        cookie.setHttpOnly(true);
//        response.addCookie(cookie);
//        response.setHeader(CONTENT_TYPE, JSON_TYPE);
//        response.setContentType(MimeTypeConstant.FORM_URLENCODED_TYPE);
//        response.setHeader("bbb=key", "12345");
//        response.setHeader("token", "12345667");
//        response.write("test");
//        response.close();
//        // 允许出现的字符 # $ % - +
//        // 非法字符 ( ) =
//        response.setHeader("asdasd/123","asd");
//        HttpSession session = request.getSession();
//        session.setAttribute("sessino1,", "val1");
//        response.setCharacterEncoding("utf-8");
    }
}
