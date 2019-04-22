package org.aron.server.core.holder;


import lombok.Data;
import org.aron.server.servlet.Servlet;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 11:43
 **/
@Data
public class ServletHolder {
    private Servlet servlet;
    private Class<?> servletClass;

    public ServletHolder(Class<?> servletClass) {
        this.servletClass = servletClass;
    }
}
