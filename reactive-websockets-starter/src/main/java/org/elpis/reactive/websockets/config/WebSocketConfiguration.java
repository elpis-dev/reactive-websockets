package org.elpis.reactive.websockets.config;

import org.elpis.reactive.websockets.config.event.ClosedConnectionHandlerConfiguration;
import org.elpis.reactive.websockets.config.event.EventManagerConfiguration;
import org.elpis.reactive.websockets.config.handler.BaseWebSocketHandler;
import org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerFunction;
import org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerFunctions;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;
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

import java.util.*;
import java.util.stream.Stream;

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
                                         final WebSocketHandlerFunction webSocketHandlerFunction,
                                         final WebSessionRegistry registry) {

        final Map<String, WebSocketHandler> handlerMap = new HashMap<>();

        final List<BaseWebSocketHandler> routeHandlers = this.getRouteHandlers(webSocketHandlerFunction, registry);

        Stream.concat(annotatedHandlers.stream(), routeHandlers.stream())
                .forEach(handler -> {
                    if (handlerMap.putIfAbsent(handler.getPathTemplate(), handler) != null) {
                        throw new WebSocketMappingException("WebSocketHandler with path %s was already registered", handler.getPathTemplate());
                    }
                });

        return new SimpleUrlHandlerMapping(handlerMap, HANDLER_ORDER);
    }

    private List<BaseWebSocketHandler> getRouteHandlers(final WebSocketHandlerFunction webSocketHandlerFunction,
                                                        final WebSessionRegistry registry) {

        final List<BaseWebSocketHandler> handlers = new ArrayList<>();

        WebSocketHandlerFunction function = webSocketHandlerFunction;
        while (function instanceof WebSocketHandlerFunctions.CombinedRouterFunction combinedRouterFunction) {
            final BaseWebSocketHandler webSocketHandler = combinedRouterFunction.register(registry);
            if (webSocketHandler == null) {
                break;
            }

            handlers.add(webSocketHandler);
            function = combinedRouterFunction.getHandlerFunction();
        }

        Optional.ofNullable(function.register(registry))
                .ifPresent(handlers::add);

        return handlers;
    }

}
