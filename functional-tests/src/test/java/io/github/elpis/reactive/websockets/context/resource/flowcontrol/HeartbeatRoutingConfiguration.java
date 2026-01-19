package io.github.elpis.reactive.websockets.context.resource.flowcontrol;

import static io.github.elpis.reactive.websockets.handler.route.ReactiveWebSocketHandlerFunctions.handle;

import io.github.elpis.reactive.websockets.handler.route.ReactiveWebSocketHandlerFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
public class HeartbeatRoutingConfiguration {

  @Bean
  public ReactiveWebSocketHandlerFunction pingRouting() {
    return handle(
            "/ping/routing/returns",
            true,
            1000L,
            1000L,
            (context, messages) -> {
              return Flux.empty();
            })
        .and(
            handle(
                "/ping/routing/void",
                true,
                1000L,
                1000L,
                (context, messages) -> {
                  // do nothing
                }))
        .handle(
            "/ping/routing/void/internal",
            true,
            1000L,
            1000L,
            (context, messages) -> {
              // do nothing
            })
        .handle(
            "/ping/routing/returns/internal",
            true,
            1000L,
            1000L,
            (context, messages) -> {
              return Flux.empty();
            });
  }
}
