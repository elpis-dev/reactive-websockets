package org.elpis.reactive.websockets.context.resource.connection;

import org.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import org.elpis.reactive.websockets.config.Mode;
import org.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static org.elpis.reactive.websockets.handler.route.WebSocketHandlerFunctions.handle;

@Configuration
public class CloseRoutingConfiguration {

    @Bean
    public WebSocketHandlerFunction closeRouting(final WebSocketSessionRegistry sessionRegistry) {
        return handle("/close", Mode.SHARED, (context, messages) -> {
            return Flux.fromIterable(List.of("One, Two", "Three"))
                    .delayElements(Duration.ofSeconds(1));
//                    .then(Mono.fromRunnable(() -> sessionRegistry.close(context.getSessionId(), CloseStatus.NORMAL)));
        });
    }

}
