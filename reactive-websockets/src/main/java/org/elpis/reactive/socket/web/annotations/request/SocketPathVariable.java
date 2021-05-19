package org.elpis.reactive.socket.web.annotations.request;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketPathVariable {

    @AliasFor("name")
    String value();

    @AliasFor("value")
    String name() default "";

    boolean required() default true;

}