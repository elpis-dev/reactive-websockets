package io.github.elpis.reactive.websockets.context.resource.connection;

import static io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunctions.handle;

import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
public class CloseRoutingConfiguration {

  @Bean
  public WebSocketHandlerFunction closeRouting() {
    return handle(
        "/close",
        (context, messages) -> {
          return Flux.fromIterable(List.of("One, Two", "Three"))
              .delayElements(Duration.ofSeconds(1));
        });
  }
}
