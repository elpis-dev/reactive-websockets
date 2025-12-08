package io.github.elpis.reactive.websockets.config.handler.route;

import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerRouteResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class WebSocketRouteConfiguration {
    @Bean
    public WebSocketHandlerRouteResolver webSocketHandlerRouteResolver(final WebSocketEventManagerFactory eventManagerFactory,
                                                                       final WebSocketSessionRegistry sessionRegistry,
                                                                       final List<WebSocketHandlerFunction> handlerFunctions) {

        return new WebSocketHandlerRouteResolver(eventManagerFactory, sessionRegistry, handlerFunctions);
    }
}
