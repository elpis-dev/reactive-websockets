package org.elpis.reactive.socket.web.annotations.controller;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
public @interface SocketResource {
    String value() default "";
}
