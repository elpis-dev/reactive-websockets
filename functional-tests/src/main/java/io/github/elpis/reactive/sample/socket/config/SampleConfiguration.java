package io.github.elpis.reactive.sample.socket.config;

import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import io.github.elpis.reactive.websockets.config.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

import static io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunctions.handle;

@Configuration
public class SampleConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SampleConfiguration.class);

    @Bean
    public WebSocketHandlerFunction webSocketHandlerFunction() {
        return handle("/ws/chat/listen", Mode.BROADCAST, (context, messageFlux) -> {
            final String userName = context.getHeader("userName", "", String.class)
                    .orElse("Not Found");

            final Integer lastMessages = context.getQueryParam("last", "1", Integer.class)
                    .orElse(1);

            final long chatId = context.getPathVariable("chatId", long.class)
                    .orElse(1L);

            return Flux.interval(Duration.ofSeconds(5))
                    .share()
                    .takeLast(lastMessages)
                    .map(i -> Map.of("chatId", chatId, "message", i, "userName", userName));
        }).handle("/ws/chat/listen/me", Mode.BROADCAST, (context, messageFlux) -> {
            messageFlux.map(WebSocketMessage::getPayloadAsText)
                    .subscribe(log::info);
        });
    }
}
