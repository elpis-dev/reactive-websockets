package io.github.elpis.reactive.websockets.context.routing;

import static io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunctions.handle;

import io.github.elpis.reactive.websockets.handler.route.WebSocketHandlerFunction;
import io.github.elpis.reactive.websockets.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Mono;

@Configuration
public class RoutingConfiguration {
  private static final Logger log = LoggerFactory.getLogger(RoutingConfiguration.class);

  @Bean
  public WebSocketHandlerFunction handlerFunction() {
    final WebSocketHandlerFunction publish =
        handle(
                "/routing/publish",
                (context, messages) -> {
                  final String headerValue = context.getHeader("id", "", String.class).orElse("");
                  return Mono.just(MessageUtils.textMessage(headerValue));
                })
            .handle(
                "/routing/connect",
                (context, messages) -> {
                  final String header = context.getHeader("id", null, String.class).orElse("");
                  log.info("Connected with header {}", header);
                });

    final WebSocketHandlerFunction listen =
        handle(
                "/routing/listen",
                (context, messages) -> {
                  messages
                      .map(WebSocketMessage::getPayloadAsText)
                      .subscribe((message) -> log.info("Received {} from '/routing/get'", message));
                })
            .handle(
                "/routing/publish/test",
                (context, messages) -> {
                  final String headerValue = context.getHeader("id", "", String.class).orElse("");
                  return Mono.just(MessageUtils.textMessage(headerValue));
                });

    return publish.and(listen);
  }
}
