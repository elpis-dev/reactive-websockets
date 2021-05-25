package org.elpis.reactive.websockets.web.annotations.request;

import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Order(5)
@Documented
public @interface SocketMessageBody {
}