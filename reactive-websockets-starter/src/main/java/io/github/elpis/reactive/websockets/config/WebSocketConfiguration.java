package io.github.elpis.reactive.websockets.config;

import io.github.elpis.reactive.websockets.config.event.WebSocketEventConfiguration;
import io.github.elpis.reactive.websockets.config.handler.route.WebSocketRouteConfiguration;
import io.github.elpis.reactive.websockets.exception.WebSocketMappingException;
import io.github.elpis.reactive.websockets.handler.BaseWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunctions;
import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerRouteResolver;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebSession;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Configuration class that setups all the websocket endpoints and processes annotated methods.
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@SuppressWarnings("SpringComponentScan")
@Configuration
@Import({WebSocketSessionRegistry.class,
        WebSocketEventConfiguration.class,
        WebSocketRouteConfiguration.class})
@ComponentScan("io.github.elpis.reactive.websockets.generated")
public class WebSocketConfiguration {

    private static final int HANDLER_ORDER = 10;

    @Bean
    @ConditionalOnMissingBean(WebSocketHandlerFunction.class)
    public WebSocketHandlerFunction webSocketRouterFunction() {
        return WebSocketHandlerFunctions.empty();
    }

    @Bean
    public WebFilter sessionFilter() {
        return (exchange, chain) -> chain.filter(exchange)
                .contextWrite(Context.of("sessionId", exchange.getSession().map(WebSession::getId)));
    }

    /**
     * {@link HandlerMapping} bean with all {@link SocketMapping @SocketMapping} resource.
     *
     * @return {@link HandlerMapping}
     * @since 1.0.0
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
