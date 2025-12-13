package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.context.model.TestChatMessage;
import io.github.elpis.reactive.websockets.context.model.TestMessage;
import io.github.elpis.reactive.websockets.context.model.TestUserMessage;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@MessageEndpoint("/json")
public class JsonBodySocketResource {
  private static final Logger log = LoggerFactory.getLogger(JsonBodySocketResource.class);

  // Simple type - String with Flux
  @OnMessage(value = "/string", mode = Mode.BROADCAST)
  public Flux<String> receiveString(@RequestBody final Flux<String> messages) {
    return messages.doOnNext(msg -> log.info("String: {}", msg))
            .map(msg -> "String: " + msg);
  }

  // Simple type - String with Mono
  @OnMessage(value = "/string/mono", mode = Mode.BROADCAST)
  public Mono<String> receiveStringSingle(@RequestBody final Mono<String> message) {
    return message
        .doOnNext(msg -> log.info("String (Mono): {}", msg))
        .map(msg -> "String (Mono): " + msg);
  }

  // Simple type - Integer with Flux
  @OnMessage(value = "/integer", mode = Mode.BROADCAST)
  public Flux<String> receiveInteger(@RequestBody final Flux<Integer> messages) {
    return messages.doOnNext(num -> log.info("Integer: {}", num))
            .map(num -> "Integer: " + num);
  }

  // Simple type - Integer with Mono
  @OnMessage(value = "/integer/mono", mode = Mode.BROADCAST)
  public Mono<String> receiveIntegerSingle(@RequestBody final Mono<Integer> message) {
    return message
        .doOnNext(num -> log.info("Integer (Mono): {}", num))
        .map(num -> "Integer (Mono): " + num);
  }

  // Simple type - Long with Flux
  @OnMessage(value = "/long", mode = Mode.BROADCAST)
  public Flux<String> receiveLong(@RequestBody final Flux<Long> messages) {
    return messages.doOnNext(num -> log.info("Long: {}", num))
            .map(num -> "Long: " + num);
  }

  // Simple type - Boolean with Flux
  @OnMessage(value = "/boolean", mode = Mode.BROADCAST)
  public Flux<String> receiveBoolean(@RequestBody final Flux<Boolean> messages) {
    return messages.doOnNext(bool -> log.info("Boolean: {}", bool))
            .map(bool -> "Boolean: " + bool);
  }

  // Simple POJO - MessageDto with Flux
  @OnMessage(value = "/message/flux", mode = Mode.BROADCAST)
  public Flux<String> receiveMessageFlux(@RequestBody final Flux<TestMessage> messages) {
    return messages.doOnNext(msg -> log.info("Message: {}", msg))
            .map(msg -> "Message: " + msg);
  }

  // Simple POJO - MessageDto with Mono
  @OnMessage(value = "/message/mono", mode = Mode.BROADCAST)
  public Mono<String> receiveMessageMono(@RequestBody final Mono<TestMessage> message) {
    return message
        .doOnNext(msg -> log.info("Single Message: {}", msg))
        .map(msg -> "Single Message: " + msg);
  }

  // Nested POJO - UserMessageDto with Flux
  @OnMessage(value = "/user-message", mode = Mode.BROADCAST)
  public Flux<String> receiveUserMessage(@RequestBody final Flux<TestUserMessage> messages) {
    return messages
        .doOnNext(msg -> log.info("User Message: {}", msg))
        .map(msg -> "User Message: " + msg);
  }

  // Nested POJO - UserMessageDto with Mono
  @OnMessage(value = "/user-message/mono", mode = Mode.BROADCAST)
  public Mono<String> receiveUserMessageSingle(@RequestBody final Mono<TestUserMessage> message) {
    return message
        .doOnNext(msg -> log.info("User Message (Mono): {}", msg))
        .map(msg -> "User Message (Mono): " + msg);
  }

  // POJO with Collections - ChatMessageDto
  @OnMessage(value = "/chat-message", mode = Mode.BROADCAST)
  public Flux<String> receiveChatMessage(@RequestBody final Flux<TestChatMessage> messages) {
    return messages
        .doOnNext(msg -> log.info("Chat Message: {}", msg))
        .map(msg -> "Chat Message: " + msg);
  }

  // Backward compatibility - WebSocketMessage with Flux
  @OnMessage(value = "/raw", mode = Mode.BROADCAST)
  public Flux<String> receiveRaw(@RequestBody final Flux<WebSocketMessage> messages) {
    return messages
        .doOnNext(msg -> log.info("Raw: {}", msg.getPayloadAsText()))
        .map(msg -> "Raw: " + msg.getPayloadAsText());
  }

  // Backward compatibility - WebSocketMessage with Mono
  @OnMessage(value = "/raw/mono", mode = Mode.BROADCAST)
  public Mono<String> receiveRawSingle(@RequestBody final Mono<WebSocketMessage> message) {
    return message
        .doOnNext(msg -> log.info("Raw (Mono): {}", msg.getPayloadAsText()))
        .map(msg -> "Raw (Mono): " + msg.getPayloadAsText());
  }
}
