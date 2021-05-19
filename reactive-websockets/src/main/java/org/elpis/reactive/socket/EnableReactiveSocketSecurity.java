package org.elpis.reactive.socket;

import org.elpis.reactive.socket.security.WebSocketSecurityConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(WebSocketSecurityConfiguration.class)
public @interface EnableReactiveSocketSecurity {
}
