package org.elpis.reactive.websockets.config;

import org.elpis.reactive.websockets.config.event.ClosedConnectionHandlerConfiguration;
import org.elpis.reactive.websockets.config.event.EventManagerConfiguration;
import org.elpis.reactive.websockets.config.handler.BaseWebSocketHandler;
import org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerFunction;
import org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerFunctions;
import org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerRouteResolver;
import org.elpis.reactive.websockets.exception.WebSocketMappingException;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Configuration class that setups all the websocket endpoints and processes annotated methods.
 *
 * @author Alex Zharkov
 * @see org.springframework.context.annotation.Configuration
 * @since 0.1.0
 */
@Configuration
@Import({EventManagerConfiguration.class, ClosedConnectionHandlerConfiguration.class, WebSocketHandlerRouteResolver.class})
@ComponentScan("org.elpis.reactive.websockets.generated")
public class WebSocketConfiguration {

    private static final int HANDLER_ORDER = 10;

    @Bean
    @ConditionalOnMissingBean(WebSocketHandlerFunction.class)
    public WebSocketHandlerFunction webSocketRouterFunction() {
        return WebSocketHandlerFunctions.empty();
    }

    /**
     * {@link HandlerMapping} bean with all {@link SocketMapping @SocketMapping} resource.
     *
     * @return {@link HandlerMapping}
     * @since 0.1.0
     */
    @Bean
    public HandlerMapping handlerMapping(final List<BaseWebSocketHandler> annotatedHandlers,
                                         final WebSocketHandlerRouteResolver routeResolver) {

        final Map<String, WebSocketHandler> handlerMap = new HashMap<>();

        final List<BaseWebSocketHandler> routeHandlers = routeResolver.resolve();
        Stream.concat(annotatedHandlers.stream(), routeHandlers.stream())
                .forEach(handler -> {
                    if (handlerMap.putIfAbsent(handler.getPathTemplate(), handler) != null) {
                        throw new WebSocketMappingException("WebSocketHandler with path %s was already registered", handler.getPathTemplate());
                    }
                });

        return new SimpleUrlHandlerMapping(handlerMap, HANDLER_ORDER);
    }

}
