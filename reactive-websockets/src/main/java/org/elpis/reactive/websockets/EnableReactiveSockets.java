package org.elpis.reactive.websockets;

import org.elpis.reactive.websockets.config.WebSocketConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Imports {@link WebSocketConfiguration} class
 *
 * @author Alex Zharkov
 * @since 0.1.0
 * @see org.springframework.context.annotation.Configuration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(WebSocketConfiguration.class)
public @interface EnableReactiveSockets {
}
