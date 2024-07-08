package org.elpis.reactive.websockets.config.handler.route;

import org.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import org.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import org.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import org.elpis.reactive.websockets.handler.route.WebSocketHandlerRouteResolver;
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
