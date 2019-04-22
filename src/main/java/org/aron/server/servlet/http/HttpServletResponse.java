package org.aron.server.servlet.http;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aron.commons.io.FileUtils;
import org.aron.commons.utils.ByteUtils;
import org.aron.commons.utils.PropertyUtils;
import org.aron.commons.utils.Utils;
import org.aron.server.error.ServletException;
import org.aron.server.servlet.ServletResponse;
import org.aron.server.servlet.http.enumeration.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.aron.commons.constant.CharConstant.*;
import static org.aron.commons.constant.CharsetConstant.IOS_8859_1;
import static org.aron.commons.constant.CharsetConstant.UTF_8;
import static org.aron.commons.utils.Utils.convertCapitalize;
import static org.aron.server.servlet.constant.HttpConstant.*;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 11:18
 **/
@Slf4j
public class HttpServletResponse implements ServletResponse {
    /**
     * 响应报文
     */
    private StringBuilder headerAppender;

    /**
     * 响应报文集合
     */
    private Map<String, String> headers;

    /**
     * 设置响应状态
     */
    private HttpStatus status = HttpStatus.OK;

    /**
     * 设置http协议版本号
     */
    @Setter
    private String HttpVersion = HTTP_VERSION;

    /**
     * 设置响应编码类型
     */
    @Setter
    private String characterEncoding = IOS_8859_1;

    /**
     * 设置响应报文类型
     */
    @Setter
    @Getter
    private String contentType = DEFAULT_CONTENT_TYPE;

    @Setter
    private byte[] body;

    private Map<String, Cookie> cookies;

    private boolean isClose;

    public HttpServletResponse() {
        this.headerAppender = new StringBuilder();
        this.headers = new HashMap<>(5);
        this.body = new byte[0];
        this.cookies = new HashMap<>(1);
        this.isClose = false;
    }

    public void addCookie(Cookie cookie) {
        if (!this.isClose) {
            cookies.put(cookie.getKey(), cookie);
        }
    }

    public void delCookie(String... keys) {
        if (this.isClose) {
            return;
        }
        if (ArrayUtils.isEmpty(keys)) {
            return;
        }
        for (String key : keys) {
            Cookie cookie = new Cookie(key, "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            this.cookies.put(key, cookie);
        }
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * bio模式返回响应报文
     * 默认不采用utf-8编码
     * @return 响应报文字节流
     */
    @Override
    public byte[] getResponseBytes() {
        return buildResponse();
    }

    // nio模式
    @Override
    public ByteBuffer[] getResponseBuffers() {
        byte[] bytes = buildResponse();
        return new ByteBuffer[]{ByteBuffer.wrap(bytes)};
    }

    private byte[] buildResponse() {
        buildHeader();
        buildBody();
        byte[] header = this.headerAppender.toString()
                .getBytes(Charset.forName(characterEncoding));
        return ByteUtils.mergeArray(header, body);
    }

    /**
     * 设置响应头信息
     * 自动过滤key="", key=null, 正则表达式[\\w-]+ 无法匹配的key
     * 自动过滤value=null
     * 请求关闭后无法设置header
     * @param key 合法的key
     * @param value 合法的值
     */
    @Override
    public void setHeader(String key, String value) {
        if (key.isEmpty() || value == null || isClose) {
            return;
        }
        if (!Pattern.matches("[\\w-]+", key)) {
            return;
        }
        this.headers.put(convertCapitalize(key), value);
    }

    public String getHeader(String key) {
        if (key.isEmpty() || isClose) {
            return null;
        }
        return this.headers.get(convertCapitalize(key));
    }

    private void buildBody() {
        this.headerAppender.append(body.length).append(CRLF)
                .append(CRLF);
    }

    private void buildHeader() {
        headerAppender
                // HTTP/1.1 200 OK  -- 报文协议及版本 、状态码及状态描述
                .append(HttpVersion)
                .append(BLANK)
                .append(status.getCode())
                .append(BLANK)
                .append(status.getMessage()).append(CRLF)
                .append(SERVER).append(COLON)
                .append(BLANK)
                .append(DEFAULT_SERVER).append(CRLF)
                //Date: Sat, 31 Dec 2005 23:59:59 GMT
                .append(DATE).append(COLON)
                .append(BLANK)
                .append(new Date()).append(CRLF);
        // Content-Type: text/html; charset=utf-8
        String contentTypeKey = convertCapitalize(CONTENT_TYPE);
        if (!headers.containsKey(contentTypeKey)) {
            String contentType = this.contentType + "; " + CHARSET + "=" + characterEncoding;
            headers.put(contentTypeKey, contentType);
        }

        if (headers != null) {
            headers.forEach((key, val) -> headerAppender.append(key)
                    .append(COLON).append(BLANK)
                    .append(val).append(CRLF));
        }
        if (cookies.size() > 0) {
            cookies.values().forEach(cookie -> headerAppender.append(cookie));
        }
        // Content-Length:
        headerAppender.append(CONTENT_LENGTH).append(COLON).append(BLANK);
    }


    @Override
    public void write(byte[] bytes) {
        if (!this.isClose) {
            this.body = ByteUtils.mergeArray(body, bytes);
        }
    }

    @Override
    public void write(String str) {
        if (Utils.isContainChinese(str)) {
            this.setCharacterEncoding(UTF_8);
        }
        byte[] bytes = str.getBytes(Charset.forName(characterEncoding));
        this.write(bytes);
    }

    @Override
    public void write(Object object) {
        this.write(JSON.toJSONString(object));
    }

    @Override
    public void close() {
        this.isClose = true;
    }

    private void writeFile(String filename, byte[] bytes, String mimeType) throws UnsupportedEncodingException {
        boolean hasSuffix = StringUtils.contains(filename, ".");
        if (StringUtils.isBlank(mimeType)) {
            if (!hasSuffix) {
                return;
            }
            mimeType = StringUtils.substringAfter(filename, ".");
        }
        mimeType = StringUtils.lowerCase(mimeType);
        String type = PropertyUtils.getProperty(this.getClass().getResourceAsStream("/mimetype.properties"), mimeType);
        if (StringUtils.isNotBlank(type)) {
            setHeader(CONTENT_TYPE, type);
            if (!hasSuffix) {
                filename = filename + mimeType;
            }
            setHeader(CONTENT_DISPOSITION, "attachment; filename="
                    + URLEncoder.encode(filename, UTF_8));
            this.write(bytes);
        }
    }

    @Override
    public void writeFile(String filename, byte[] bytes) {
        try {
            writeFile(filename, bytes, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeFile(String uri, String mimeType) throws ServletException {
        try {
            String resourcePath = FileUtils.getResourcePath();
            writeFile(StringUtils.substringAfter(uri, "/"),
                    FileUtils.readFileToByteArray(new File(resourcePath + uri)), mimeType);
        } catch (IOException e) {
            throw new ServletException(HttpStatus.NOT_FOUND);
        }
    }
}
