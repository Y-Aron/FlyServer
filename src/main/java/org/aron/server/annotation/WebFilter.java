package org.aron.server.annotation;

import java.lang.annotation.*;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 12:22
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebFilter {
    String[] value();
    String name() default "";
}
