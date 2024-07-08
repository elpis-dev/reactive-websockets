package org.elpis.reactive.websockets.context.routing;

import org.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import org.elpis.reactive.websockets.config.Mode;
import org.elpis.reactive.websockets.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Mono;

import static org.elpis.reactive.websockets.handler.route.WebSocketHandlerFunctions.handle;

@Configuration
public class RoutingConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RoutingConfiguration.class);

    @Bean
    public WebSocketHandlerFunction handlerFunction() {
        final WebSocketHandlerFunction publish = handle("/routing/publish", Mode.SHARED, (context, messages) -> {
            final String headerValue = context.getHeader("id", "", String.class)
                    .orElse("");
            return Mono.just(MessageUtils.textMessage(headerValue));
        }).handle("/routing/connect", Mode.SHARED, (context, messages) -> {
            final String headerValue = context.getHeader("id", "", String.class)
                    .orElse("");
            log.info("Connected with header {}", headerValue);
        });

        final WebSocketHandlerFunction listen = handle("/routing/listen", Mode.SHARED, (context, messages) -> {
            messages.map(WebSocketMessage::getPayloadAsText)
                    .subscribe((message) -> log.info("Received {} from '/routing/get'", message));
        }).handle("/routing/publish/test", Mode.SHARED, (context, messages) -> {
            final String headerValue = context.getHeader("id", "", String.class)
                    .orElse("");
            return Mono.just(MessageUtils.textMessage(headerValue));
        });


        return publish.and(listen);
    }
}
