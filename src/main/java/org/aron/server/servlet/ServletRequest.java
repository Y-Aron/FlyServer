package org.aron.server.servlet;

import com.alibaba.fastjson.JSONObject;
import org.aron.server.core.ServletContext;
import org.aron.server.error.ServletException;
import org.aron.server.network.handler.AbstractRequestHandler;
import org.aron.server.servlet.http.Multipart;
import org.aron.server.servlet.http.enumeration.HttpRequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 11:19
 **/
public interface ServletRequest {

    // 设置上下文
    void setServletContext(ServletContext context);

    // 设置请求处理
    void setRequestHandler(AbstractRequestHandler handler);

    void init() throws ServletException, IOException;

    Multipart getFile(String name);

    Multipart[] getFiles(String name);

    /**
     * 获取queryString 或者body(表单格式)的键值类型的数据
     * @param name 请求 name
     */
    String getParameter(String name);

    List<String> getParameters(String name);

    Map<String, List<String>> getParameterMap();

    String getUrl();

    String getProtocol();

    String getMimeType();

    HttpRequestMethod getMethod();

    String getHeader(String name);

    String[] getHeaders(String name);

    JSONObject getJsonBody();
}
