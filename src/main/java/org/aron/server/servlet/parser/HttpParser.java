package org.aron.server.servlet.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aron.commons.constant.CharConstant;
import org.aron.commons.constant.CharsetConstant;
import org.aron.commons.utils.ByteUtils;
import org.aron.commons.utils.Utils;
import org.aron.server.servlet.http.Cookie;
import org.aron.server.servlet.http.Multipart;
import org.aron.server.servlet.http.enumeration.HttpRequestMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

import static org.aron.commons.io.FileUtils.heapSpaceOverflow;
import static org.aron.server.servlet.constant.HttpConstant.*;


/**
 * POST /test/12/admin/123?passwd=%E6%88%91%E6%98%AF%E5%AF%86%E7%A0%81&username=%E7%94%A8%E6%88%B7 HTTP/1.1
 * Content-Type: multipart/form-data; boundary=--------------------------166503996937123157581824
 * cache-control: no-cache
 * Postman-Token: 0b401594-66bf-4e90-a7fe-1a21f5113a02
 * User-Agent: PostmanRuntime/7.4.0
 * Accept: *\/*
 * Host: 127.0.0.1:8080
 * cookie: JSESSIONID=33335407467048960
 * accept-encoding: gzip, deflate
 * content-length: 1784040
 * Connection: keep-alive
 * /
/**
 * @author: Y-Aron
 * @create: 2019-01-31 16:02
 **/
@Slf4j
public class HttpParser {

    @Setter
    @Getter
    private long maxSize;

    @Setter
    private String uploadDir;

    private byte[] beforeBytes;

    private ByteArrayOutputStream body;

    @Getter
    private String url;

    @Getter
    private String boundary;

    private Socket socket;
    private SocketChannel socketChannel;

    @Getter
    private String httpVersion;

    @Getter
    private String contentType;

    @Getter
    private long contentLength;

    @Getter
    private HttpRequestMethod method;

    private boolean headerComplete;
    @Getter
    private Cookie[] cookies;

    @Getter
    private Map<String, Object> headers;

    @Getter
    private JSONObject jsonBody;

    private MultipartParser multipartParser;

    @Getter
    private Map<String, List<String>> parameterMap;

    private static final int INIT_SIZE = 4 * 1024;
    private static final int MAX_SIZE = 64 * 1024;

    private void init() {
        this.httpVersion = HTTP_VERSION;
        this.headers = new HashMap<>(3);
        this.parameterMap = new HashMap<>(0);
        this.cookies = new Cookie[0];
    }

    public HttpParser(Socket socket) {
        this.socket = socket;
        init();
    }

    public HttpParser(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        init();
    }

    public Multipart getFile(String name) {
        Multipart[] multipartList = getFiles(name);
        if (ArrayUtils.isNotEmpty(multipartList)) {
            return multipartList[0];
        }
        return null;
    }

    /**
     * 获取上传文件列表
     * @param name 字段名称
     */
    public Multipart[] getFiles(String name) {
        if (this.multipartParser == null || MapUtils.isEmpty(this.multipartParser.getMultipartMap())) {
            return null;
        }
        List<Multipart> multipartList = new ArrayList<>(0);
        for (Multipart multipart : this.multipartParser.getMultipartMap().values()) {
            if (multipart.getName().equals(name)) {
                multipartList.add(multipart);
            }
        }
        Multipart[] array = new Multipart[multipartList.size()];
        return multipartList.toArray(array);
    }

    public boolean readRequest() throws IOException {
        log.debug("----------开始读取http request字节流----------");
        InputStream in = null;
        if (this.socket != null) {
            // socket
            in = this.socket.getInputStream();
        }
        byte[] bytes = new byte[INIT_SIZE];
        ByteBuffer byteBuffer = ByteBuffer.allocate(INIT_SIZE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        boolean finished = true;
        int len;
        long total = 0;
        while (finished) {
            if (in != null && (len = in.read(bytes)) > 0) {
                // socket
                total += len;
                readBytes(bos, bytes, len);
            }
            if (this.socketChannel != null && (len = this.socketChannel.read(byteBuffer)) > 0) {
                // socketChannel
                total += len;
                byteBuffer.flip();
                readBytes(bos, byteBuffer.array(), len);
                byteBuffer.clear();
            }
            if (bos.size() > 0 && !this.headerComplete) {
                parseHttpHeader(bos.toString(CharsetConstant.IOS_8859_1), bos);
                if (this.contentLength > this.maxSize) {
                    return false;
                }
                if (this.contentLength >= INIT_SIZE) {
                    bytes = new byte[MAX_SIZE];
                    byteBuffer = ByteBuffer.allocate(MAX_SIZE);
                }
                // 初始化文件上传解析器
                if (StringUtils.isNotBlank(contentType) && contentType.startsWith(MULTIPART_TYPE)) {
                    multipartParser = new MultipartParser(boundary, uploadDir);
                }
            }
            if (isBodyComplete(total, bos)) {
                log.debug("body is complete");
                putCache(bos, true);
                // 关闭字节数组输入流
                bos.close();
                finished = false;
            }
        }
        log.debug("----------读取http request字节流完毕----------");
//        show(total);
        return true;
    }

    public void buildBody() throws IOException {
        log.debug("----------开始构建请求体信息----------");
        if (StringUtils.isBlank(contentType) || body == null) {
            return;
        }
        byte[] bytes = body.toByteArray();
        if (contentType.startsWith(FORM_URLENCODED_TYPE)) {
            parseParams(StringUtils.trim(new String(bytes, CharsetConstant.UTF_8_CHARSET)));
            log.debug("http servletRequest url parameterMap: {}", this.parameterMap);
        } else if (contentType.startsWith(JSON_TYPE)) {
            log.debug("----------解析JSON数据----------");
            this.jsonBody = JSON.parseObject(bytes, JSONObject.class);
            log.debug("json body: {}", this.jsonBody);
        }
        body.close();
        log.debug("----------构建请求体信息完毕----------");
    }

    private void show(long total) {
        log.debug("----------开始显示读取信息----------");
        log.debug("read total: {}", total);
        log.debug("content length: {}", this.contentLength);
        log.debug("read total - content length: {}", total - contentLength);
        multipartParser.getMultipartMap().forEach((key, val) -> log.debug("Multipart key: {}, val: {}", key, val));
        log.debug("----------显示读取信息完毕----------");
    }

    private void readBytes(ByteArrayOutputStream bos, byte[] bytes, int len) throws IOException {
        log.debug("----------开始读取字节流----------");
        if (this.headerComplete) {
            putCache(bos, false);
        }
        if (heapSpaceOverflow(bos.size() + MAX_SIZE)) {
            log.debug("----------bos size 即将超出jvm内存 优先进行解析----------");
            putCache(bos, false);
            log.debug("bos size: {}", bos.size());
        }
        bos.write(bytes, 0, len);
        log.debug("----------读取字节流完毕----------");
    }

    private void putCache(ByteArrayOutputStream bos, boolean finished) throws IOException {
        log.debug("----------开始写入缓存----------");
        log.debug("body size: {}", bos.size());
        // 文件上传
        if (StringUtils.startsWithIgnoreCase(contentType, MULTIPART_TYPE)) {
            if (heapSpaceOverflow(bos.size()) || finished) {
                byte[] bytes = bos.toByteArray();
                if (beforeBytes != null && bytes.length < ("--" +boundary + "--").getBytes().length) {
                    beforeBytes = ByteUtils.mergeArray(beforeBytes, bytes);
                } else {
                    beforeBytes = bytes;
                }
                multipartParser.parseMultipart(bytes);
                bos.reset();
            }
        } else {
            if (body == null) {
                body = new ByteArrayOutputStream();
            }
            body.write(bos.toByteArray());
        }
        log.debug("----------写入缓存完毕----------");
    }

    /**
     * 判断请求数据流是否接收完整
     * 根据 当前字节数组长度 和 boundary进行判断
     * @param size 字节数组长度
     */
    private boolean isBodyComplete(long size, ByteArrayOutputStream body) {
        log.debug("----------判断请求体是否读取成功----------");
        log.debug("size: {}, content length: {}", size, this.contentLength);
        if (size < this.contentLength) {
            return false;
        }
        if (StringUtils.isNotBlank(boundary)) {
            int lastBoundarySize = ("--" + boundary + "--").getBytes().length;
            log.debug("last boundary size: {}", lastBoundarySize);
            log.debug("boundary: {}", boundary);
            log.debug("body size: {}", body.size());
            byte[] bytes = body.toByteArray();
            if (body.size() < lastBoundarySize) {
                log.debug("body size < last boundary size");
                bytes = ByteUtils.mergeArray(beforeBytes, bytes);
            }
            long total = bytes.length;
            long starIndex = total - 2 - lastBoundarySize;
            String lastLine = new String(bytes, (int) starIndex, lastBoundarySize, CharsetConstant.IOS_8859_1_CHARSET);
            log.debug("last line: {}", lastLine);
            return lastLine.contains("--" + boundary + "--");
        }
        return true;
    }

    /**
     * 解析请求头的字符串
     * @param request 请求头信息的字符串
     */
    private void parseHttpHeader(String request, ByteArrayOutputStream bos) throws UnsupportedOperationException, IOException {
        String[] topArray = request.split(CharConstant.CRLF)[0].split(CharConstant.BLANK);

        if (!checkHttpMethod(topArray[0])) {
            throw new UnsupportedOperationException();
        }
        this.url = topArray[1];
        this.httpVersion = topArray[2];
        log.debug("http method: {}", this.method);
        log.debug("http url: {}", this.url);
        log.debug("http version: {}", this.httpVersion);
        int pos = request.indexOf(CharConstant.DOUBLE_CRLF);

        if (pos > 0) {
            this.headerComplete = true;
            // 解析请求头信息
            parseHeaders(request.substring(0, pos));
            if (getHeader(CONTENT_LENGTH) != null) {
                this.contentLength = Long.parseLong(getHeader(CONTENT_LENGTH));
            }
            byte[] bytes = request.substring(pos + CharConstant.DOUBLE_CRLF.getBytes().length)
                    .getBytes(CharsetConstant.IOS_8859_1_CHARSET);
            bos.reset();
            bos.write(bytes);
        }
        log.debug("header is complete: {}", request.contains(CharConstant.DOUBLE_CRLF));
    }

    /**
     * 解析请求头
     * @param rawHeader 请求头行
     */
    private void parseHeaders(String rawHeader) throws UnsupportedEncodingException {
        log.debug("----------解析请求头信息----------");
        // 解析方法
        String[] headerArray = rawHeader.split(CharConstant.CRLF);
        // 解析URL
        int pos = this.url.lastIndexOf('?');
        if (pos > 0) {
            // 解析URL参数
            parseParams(this.url.substring(pos + 1));
            // 存在URL参数
            this.url = this.url.substring(0, pos);
        }
        this.url = URLDecoder.decode(this.url, CharsetConstant.UTF_8);
        log.debug("url: {}", this.url);
        log.debug("http servletRequest url parameterMap: {}", this.parameterMap);

        // 解析请求头信息
        String header;
        for (int i = 1, len =  headerArray.length; i < len; i++) {
            header = headerArray[i];
            if (StringUtils.isBlank(header)) {
                break;
            }
            int colonIndex = header.indexOf(CharConstant.COLON);
            String key = header.substring(0, colonIndex);
            String value = header.substring(colonIndex + 2);
            // content-type
            if (key.equalsIgnoreCase(CONTENT_TYPE)) {
                parseContentType(value);
                continue;
            }
            // cookie
            if (key.equalsIgnoreCase(COOKIE)) {
                parseCookies(value);
                continue;
            }
            if (value.contains(",")) {
                this.headers.put(Utils.convertCapitalize(key), StringUtils.split(value, ','));
            } else {
                this.headers.put(Utils.convertCapitalize(key), value);
            }
        }
        this.headers.forEach((key, val) -> {
            log.debug("header key: {}, value: {}", key, val);
        });
    }

    /**
     * 解析 Cookie
     * cookie: Cookie_2=value;
     */
    private void parseCookies(String rawCookie) {
        log.debug("----------解析cookies----------");
        String[] cookieArray = rawCookie.split(CharConstant.COABLANK);

        this.cookies = new Cookie[cookieArray.length];

        for (int i = 0; i < cookieArray.length; i++) {
            this.cookies[i] = new Cookie(StringUtils.substringBefore(cookieArray[i], "="),
                    StringUtils.substringAfter(cookieArray[i], "="));
        }
        log.debug("cookies: {}", (Object) this.cookies);
    }

    public String getHeader(String key) {
        Object value = verifyAndGetHeader(key);
        if (value == null) {
            return null;
        }
        if (value.getClass().equals(String[].class)) {
            String[] strings = (String[]) value;
            return ArrayUtils.isNotEmpty(strings)? strings[0] : null;
        }
        return (String) value;
    }

    public String[] getHeaders(String key) {
        Object value = verifyAndGetHeader(key);
        if (value == null) {
            return null;
        }
        if (value.getClass().equals(String.class)) {
            return new String[]{(String) value};
        }
        return (String[]) value;
    }

    /**
     * 解析表单编码的数据
     * name=value&name1=value1
     * @param params 参数字符串
     */
    private void parseParams(String params) throws UnsupportedEncodingException {
        log.debug("----------解析表单参数----------");
        params = URLDecoder.decode(params, CharsetConstant.UTF_8);
        log.debug("表单参数字符串：{}", params);
        String[] urlParams = params.split("&");
        for (String param: urlParams) {
            int pos = param.indexOf("=");
            if (pos != -1) {
                String key = param.substring(0, pos);
                String[] value = param.substring(pos + 1).split(",");
                List<String> list = new ArrayList<>(Arrays.asList(value));
                if (parameterMap.containsKey(key)) {
                    parameterMap.get(key).addAll(list);
                } else {
                    parameterMap.put(key, list);
                }
            }
        }
        log.debug("----------解析表单参数完毕！----------");
    }

    /**
     * 解析content-type 字符串
     * @param rawContentType content-type 字符串
     */
    private void parseContentType(String rawContentType) {
        this.contentType = StringUtils.substringBefore(rawContentType, CharConstant.COABLANK);
        String tmp = StringUtils.substringAfter(rawContentType, CharConstant.COABLANK);
        if (tmp.toLowerCase().contains(MULTIPART_BOUNDARY)) {
            this.boundary = StringUtils.substringAfter(tmp, "=");
        }
        log.debug("contentType: {}", contentType);
        log.debug("boundary: {}", boundary);
    }

    /**
     * 检验请求类型是否匹配
     */
    private boolean checkHttpMethod(String httpMethod) {
        for (HttpRequestMethod method : HttpRequestMethod.values()) {
            if (httpMethod.equalsIgnoreCase(method.getName())) {
                this.method = method;
                return true;
            }
        }
        return false;
    }

    private Object verifyAndGetHeader(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return this.headers.get(Utils.convertCapitalize(key));
    }
}
