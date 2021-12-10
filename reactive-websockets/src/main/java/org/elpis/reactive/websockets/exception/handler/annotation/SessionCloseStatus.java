package org.elpis.reactive.websockets.exception.handler.annotation;

import org.elpis.reactive.websockets.config.model.WebSocketCloseStatus;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionCloseStatus {

    WebSocketCloseStatus[] value() default { };

    int[] code() default { };

}
