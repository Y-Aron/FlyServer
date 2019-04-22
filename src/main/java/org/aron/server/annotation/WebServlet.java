package org.aron.server.annotation;

import java.lang.annotation.*;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 12:20
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebServlet {
    String[] value();
    String name() default "";
}
