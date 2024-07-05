package org.elpis.reactive.websockets.context.resource.connection;

import org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerFunction;
import org.elpis.reactive.websockets.config.model.Mode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import static org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerFunctions.handle;

@Configuration
public class PingRoutingConfiguration {

    @Bean
    public WebSocketHandlerFunction pingRouting() {
        return handle("/ping/routing/returns", Mode.SHARED, true, 1000L, (context, messages) -> {
            return Flux.empty();
        }).and(handle("/ping/routing/void", Mode.SHARED, true, 1000L, (context, messages) -> {
            //do nothing
        })).handle("/ping/routing/void/internal", Mode.SHARED, true, 1000L, (context, messages) -> {
            //do nothing
        }).handle("/ping/routing/returns/internal", Mode.SHARED, true, 1000L, (context, messages) -> {
            return Flux.empty();
        });
    }
}
