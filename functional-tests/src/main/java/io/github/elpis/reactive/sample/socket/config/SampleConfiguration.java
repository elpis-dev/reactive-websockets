package io.github.elpis.reactive.sample.socket.config;

import static io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunctions.handle;

import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

@Configuration
public class SampleConfiguration {
  private static final Logger log = LoggerFactory.getLogger(SampleConfiguration.class);

  @Bean
  public WebSocketHandlerFunction webSocketHandlerFunction() {
    return handle(
            "/ws/chat/listen",
            (context, messageFlux) -> {
              final String userName =
                  context.getHeader("userName", "", String.class).orElse("Not Found");

              final Integer lastMessages =
                  context.getQueryParam("last", "1", Integer.class).orElse(1);

              final long chatId = context.getPathVariable("chatId", long.class).orElse(1L);

              return Flux.interval(Duration.ofSeconds(5))
                  .share()
                  .takeLast(lastMessages)
                  .map(i -> Map.of("chatId", chatId, "message", i, "userName", userName));
            })
        .handle(
            "/ws/chat/listen/me",
            (context, messageFlux) -> {
              messageFlux.map(WebSocketMessage::getPayloadAsText).subscribe(log::info);
            });
  }
}
