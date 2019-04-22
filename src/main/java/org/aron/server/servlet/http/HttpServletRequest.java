package org.aron.server.servlet.http;

import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aron.server.core.ServerConfiguration;
import org.aron.server.core.ServletContext;
import org.aron.server.network.handler.AbstractRequestHandler;
import org.aron.server.servlet.ServletRequest;
import org.aron.server.servlet.Session;
import org.aron.server.servlet.http.enumeration.HttpRequestMethod;
import org.aron.server.servlet.parser.HttpParser;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.rmi.ServerException;
import java.util.List;
import java.util.Map;

import static org.aron.server.core.ServerConfiguration.SESSION_NAME;


/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 11:18
 **/
@Slf4j
public class HttpServletRequest implements ServletRequest {

    @Setter
    private AbstractRequestHandler requestHandler;

    private HttpParser httpParser;

    @Setter
    private ServletContext servletContext;

    private HttpSession session;

    @Override
    public void init() throws IOException {
        this.httpParser.setMaxSize(getUploadSize());
        this.httpParser.setUploadDir(ServerConfiguration.UPLOAD_DIR);
        boolean isReadOk = this.httpParser.readRequest();
        if (!isReadOk) {
            throw new ServerException(String
                    .format("Content length[%s kb] exceeds the maximum upload size[%s kb], Please increase the upload file size",
                            this.httpParser.getContentLength(), this.httpParser.getMaxSize()));
        }
        this.httpParser.buildBody();
    }

    public HttpServletRequest(Socket socket) throws UnsupportedOperationException, IOException {
        this.httpParser = new HttpParser(socket);
        init();
    }

    public HttpServletRequest(SocketChannel socketChannel) throws IOException {
        this.httpParser = new HttpParser(socketChannel);
        init();
    }

    private long getUploadSize() {
        long upload_max_size = ServerConfiguration.UPLOAD_MAX_SIZE;
        String upload_unit = ServerConfiguration.UPLOAD_UNIT;
        // kb mb gb
        if ("kb".equalsIgnoreCase(upload_unit)) {
            return upload_max_size * 1024;
        } else if ("mb".equalsIgnoreCase(upload_unit)) {
            return upload_max_size * 1024 * 1024;
        } else if ("gb".equalsIgnoreCase(upload_unit)) {
            return upload_max_size * 1024 * 1024 * 1024;
        }
        return 1024;
    }

    @Override
    public Multipart getFile(String name) {
        return this.httpParser.getFile(name);
    }

    @Override
    public Multipart[] getFiles(String name) {
        return this.httpParser.getFiles(name);
    }

    /**
     * 获取 Cookie
     */
    public Cookie getCookie(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        if (ArrayUtils.isNotEmpty(this.httpParser.getCookies())) {
            for (Cookie cookie : this.httpParser.getCookies()) {
                if (key.equals(cookie.getKey())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    @Override
    public String getParameter(String name) {
        List<String> params = this.httpParser.getParameterMap().get(name);
        if (params == null) {
            return null;
        }
        return params.get(0);
    }

    @Override
    public List<String> getParameters(String name) {
        Map<String, List<String>> map = this.httpParser.getParameterMap();
        List<String> params = this.httpParser.getParameterMap().get(name);
        if (params == null) {
            return null;
        }
        return params;
    }

    public String getHeader(String name) {
        return this.httpParser.getHeader(name);
    }

    public String[] getHeaders(String name) {
        return this.httpParser.getHeaders(name);
    }

    public HttpRequestMethod getMethod() {
        return this.httpParser.getMethod();
    }

    @Override
    public Map<String, List<String>> getParameterMap() {
        return this.httpParser.getParameterMap();
    }

    @Override
    public String getUrl() {
        return this.httpParser.getUrl();
    }

    @Override
    public String getProtocol() {
        return this.httpParser.getHttpVersion();
    }

    @Override
    public String getMimeType() {
        return StringUtils.substringBefore(StringUtils.substringAfter(getUrl(), "."), "/");
    }

    public HttpSession getSession() {
        if (session != null) {
            return session;
        }
        for (Cookie cookie : this.httpParser.getCookies()) {
            if (SESSION_NAME.equals(cookie.getKey())) {
                Session currentSession = servletContext.getSession(cookie.getValue());
                if (currentSession != null) {
                    session = (HttpSession) currentSession;
                    return session;
                }
            }
        }
        session = (HttpSession) servletContext.createSession(requestHandler.getServletResponse());
        return session;
    }

    public JSONObject getJsonBody() {
        return this.httpParser.getJsonBody();
    }
}
