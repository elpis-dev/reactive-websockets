package org.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PingPong {
    long value() default 1000L;

    boolean enabled() default true;
}
