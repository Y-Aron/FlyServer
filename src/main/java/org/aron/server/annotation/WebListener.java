package org.aron.server.annotation;

import java.lang.annotation.*;

/**
 * @author: Y-Aron
 * @create: 2019-02-11 22:33
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebListener {
    String value();
}
