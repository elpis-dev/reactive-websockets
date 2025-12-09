package io.github.elpis.reactive.websockets;

import io.github.elpis.reactive.websockets.config.security.WebSocketSecurityConfiguration;
import io.github.elpis.reactive.websockets.security.SocketHandshakeService;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Imports {@link WebSocketSecurityConfiguration @Configuration WebSocketSecurityConfiguration} class. Websocket security configuration is based on two
 * aspects: {@link org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter WebSocketHandlerAdapter} bean registration with or without custom {@link SocketHandshakeService SocketHandshakeService}
 * implementation and {@link org.springframework.security.web.server.SecurityWebFilterChain}
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(WebSocketSecurityConfiguration.class)
public @interface EnableReactiveSocketSecurity {
}
