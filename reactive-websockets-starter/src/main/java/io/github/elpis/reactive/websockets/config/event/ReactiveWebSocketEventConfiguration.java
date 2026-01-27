package io.github.elpis.reactive.websockets.config.event;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration class that imports event manager and closed connection handler configurations for
 * reactive WebSocket support.
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Configuration
@Import({ReactiveEventManagerConfiguration.class, ClosedConnectionHandlerConfiguration.class})
public class ReactiveWebSocketEventConfiguration {}
