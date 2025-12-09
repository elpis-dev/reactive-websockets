package io.github.elpis.reactive.websockets.web.annotation;

import io.github.elpis.reactive.websockets.config.Mode;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMessage {

    String value();

    Mode mode();

    Ping ping() default @Ping(enabled = false);
}
