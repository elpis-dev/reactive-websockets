package org.elpis.reactive.websockets.config;

import org.elpis.reactive.websockets.config.event.ClosedConnectionHandlerConfiguration;
import org.elpis.reactive.websockets.config.event.EventManagerConfiguration;
import org.elpis.reactive.websockets.config.handler.BaseWebSocketHandler;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration class that setups all the websocket endpoints and processes annotated methods.
 *
 * @author Alex Zharkov
 * @see org.springframework.context.annotation.Configuration
 * @since 0.1.0
 */
@Configuration
@Import({EventManagerConfiguration.class, ClosedConnectionHandlerConfiguration.class})
@ComponentScan("org.elpis.reactive.websockets.generated")
public class WebSocketConfiguration {

    private static final int HANDLER_ORDER = 10;

    /**
     * {@link HandlerMapping} bean with all {@link SocketMapping @SocketMapping} resource.
     *
     * @return {@link HandlerMapping}
     * @since 0.1.0
     */
    @Bean
    public HandlerMapping handlerMapping(final List<BaseWebSocketHandler> handlers) {
        final Map<String, WebSocketHandler> handlerMap = handlers.stream()
                .collect(Collectors.toMap(BaseWebSocketHandler::getPathTemplate, handler -> handler));
        return new SimpleUrlHandlerMapping(handlerMap, HANDLER_ORDER);
    }

}
