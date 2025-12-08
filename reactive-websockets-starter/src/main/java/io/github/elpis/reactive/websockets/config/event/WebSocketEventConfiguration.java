package io.github.elpis.reactive.websockets.config.event;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventManagerConfiguration.class, ClosedConnectionHandlerConfiguration.class})
public class WebSocketEventConfiguration {
}
