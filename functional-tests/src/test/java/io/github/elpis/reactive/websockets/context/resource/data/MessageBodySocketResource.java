package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@MessageEndpoint("/body")
public class MessageBodySocketResource {
  private static final Logger log = LoggerFactory.getLogger(MessageBodySocketResource.class);

  @OnMessage(value = "/post", mode = Mode.BROADCAST)
  public void receiveDefaultMessage(
      @RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }

  // Primitive types - String with Flux
  @OnMessage(value = "/post/string", mode = Mode.BROADCAST)
  public void receiveString(@RequestBody final Flux<String> messages) {
    messages.subscribe(msg -> log.info("String: {}", msg));
  }

  // Primitive types - String with Mono
  @OnMessage(value = "/post/string/mono", mode = Mode.BROADCAST)
  public void receiveStringSingle(@RequestBody final Mono<String> message) {
    message.subscribe(msg -> log.info("String (Mono): {}", msg));
  }

  // Primitive types - Integer with Flux
  @OnMessage(value = "/post/integer", mode = Mode.BROADCAST)
  public void receiveInteger(@RequestBody final Flux<Integer> messages) {
    messages.subscribe(num -> log.info("Integer: {}", num));
  }

  // Primitive types - Integer with Mono
  @OnMessage(value = "/post/integer/mono", mode = Mode.BROADCAST)
  public void receiveIntegerSingle(@RequestBody final Mono<Integer> message) {
    message.subscribe(num -> log.info("Integer (Mono): {}", num));
  }

  // Primitive types - Long with Flux
  @OnMessage(value = "/post/long", mode = Mode.BROADCAST)
  public void receiveLong(@RequestBody final Flux<Long> messages) {
    messages.subscribe(num -> log.info("Long: {}", num));
  }

  // Primitive types - Boolean with Flux
  @OnMessage(value = "/post/boolean", mode = Mode.BROADCAST)
  public void receiveBoolean(@RequestBody final Flux<Boolean> messages) {
    messages.subscribe(bool -> log.info("Boolean: {}", bool));
  }

  @OnMessage(value = "/post/binary", mode = Mode.BROADCAST)
  public Flux<byte[]> sendBinaryMessage() {
    return Flux.just("Binary".getBytes());
  }

  @OnMessage(value = "/post/stream", mode = Mode.BROADCAST)
  public Mono<InputStream> sendStreamMessage() {
    return Mono.just(new ByteArrayInputStream("Stream".getBytes()));
  }
}
