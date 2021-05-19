package org.elpis.reactive.socket.web.annotations.request;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketMessageBody {
}