package org.aron.server.core.holder;


import lombok.Data;
import org.aron.server.servlet.Filter;

/**
 * @author: Y-Aron
 * @create: 2019-01-01 10:15
 **/
@Data
public class FilterHolder {
    private Filter filter;
    private Class<?> filterClass;

    public FilterHolder(Class<?> filterClass) {
        this.filterClass = filterClass;
    }
}
