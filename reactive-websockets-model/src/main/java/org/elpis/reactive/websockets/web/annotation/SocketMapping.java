package org.elpis.reactive.websockets.web.annotation;

import org.elpis.reactive.websockets.config.model.Mode;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketMapping {

    String value();

    Mode mode();

    PingPong pingPong() default @PingPong(enabled = false);
}
