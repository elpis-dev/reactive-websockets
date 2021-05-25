package org.elpis.reactive.websockets.web.annotations.request;

import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Order(3)
@Documented
public @interface SocketPathVariable {

    @AliasFor("name")
    String value();

    @AliasFor("value")
    String name() default "";

    boolean required() default true;

}