package io.github.elpis.reactive.websockets.context.resource.connection;

import static io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunctions.handle;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
public class PingRoutingConfiguration {

  @Bean
  public WebSocketHandlerFunction pingRouting() {
    return handle(
            "/ping/routing/returns",
            Mode.BROADCAST,
            true,
            1000L,
            1000L,
            (context, messages) -> {
              return Flux.empty();
            })
        .and(
            handle(
                "/ping/routing/void",
                Mode.BROADCAST,
                true,
                1000L,
                1000L,
                (context, messages) -> {
                  // do nothing
                }))
        .handle(
            "/ping/routing/void/internal",
            Mode.BROADCAST,
            true,
            1000L,
            1000L,
            (context, messages) -> {
              // do nothing
            })
        .handle(
            "/ping/routing/returns/internal",
            Mode.BROADCAST,
            true,
            1000L,
            1000L,
            (context, messages) -> {
              return Flux.empty();
            });
  }
}
