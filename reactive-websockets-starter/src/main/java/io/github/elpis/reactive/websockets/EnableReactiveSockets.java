package io.github.elpis.reactive.websockets;

import io.github.elpis.reactive.websockets.config.WebSocketConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Imports {@link WebSocketConfiguration} class
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(WebSocketConfiguration.class)
public @interface EnableReactiveSockets {
}
