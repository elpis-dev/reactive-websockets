package org.elpis.reactive.websockets.context.connection;

import org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerFunction;
import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.config.handler.WebSessionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.elpis.reactive.websockets.config.handler.route.WebSocketHandlerFunctions.handle;

@Configuration
public class CloseRoutingConfiguration {

    @Bean
    public WebSocketHandlerFunction closeRouting(final WebSessionRegistry sessionRegistry) {
        return handle("/close", Mode.SHARED, (context, messages) -> {
            return Flux.fromIterable(List.of("One, Two", "Three"))
                    .delayElements(Duration.ofSeconds(1))
                    .then(Mono.fromRunnable(() -> sessionRegistry.close(context.getSessionId(), CloseStatus.NORMAL)));
        });
    }

}
