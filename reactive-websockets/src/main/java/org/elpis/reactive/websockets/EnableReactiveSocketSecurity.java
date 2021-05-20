package org.elpis.reactive.websockets;

import org.elpis.reactive.websockets.security.WebSocketSecurityConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(WebSocketSecurityConfiguration.class)
public @interface EnableReactiveSocketSecurity {
}
