package org.elpis.reactive.websockets;

import org.elpis.reactive.websockets.config.WebSocketConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(WebSocketConfiguration.class)
public @interface EnableReactiveSockets {
}
