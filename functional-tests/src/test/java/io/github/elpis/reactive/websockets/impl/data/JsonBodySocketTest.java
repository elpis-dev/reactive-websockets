package io.github.elpis.reactive.websockets.impl.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.model.MessageDto;
import io.github.elpis.reactive.websockets.context.model.TestChatMessage;
import io.github.elpis.reactive.websockets.context.model.TestUserMessage;
import io.github.elpis.reactive.websockets.context.resource.data.JsonBodySocketResource;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, JsonBodySocketResource.class})
class JsonBodySocketTest extends BaseWebSocketTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testFluxStringDeserialization() throws Exception {
    // given
    final String path = "/json/string";
    final Flux<String> data = Flux.just("Hello", "World", "Test");
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asFlux().take(3))
          .expectNext("String: Hello")
          .expectNext("String: World")
          .expectNext("String: Test")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs())
          .containsSequence("String: Hello", "String: World", "String: Test");
    }
  }

  @Test
  void testMonoStringDeserialization() throws Exception {
    // given
    final String path = "/json/string/mono";
    final Mono<String> data = Mono.just("SingleMessage");
    final Sinks.One<String> sink = Sinks.one();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitValue(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asMono())
          .expectNext("String (Mono): SingleMessage")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs()).contains("String (Mono): SingleMessage");
    }
  }

  @Test
  void testFluxIntegerDeserialization() throws Exception {
    // given
    final String path = "/json/integer";
    final Flux<String> data = Flux.just("42", "100", "999");
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asFlux().take(3))
          .expectNext("Integer: 42")
          .expectNext("Integer: 100")
          .expectNext("Integer: 999")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs())
          .containsSequence("Integer: 42", "Integer: 100", "Integer: 999");
    }
  }

  @Test
  void testMonoIntegerDeserialization() throws Exception {
    // given
    final String path = "/json/integer/mono";
    final Mono<String> data = Mono.just("777");
    final Sinks.One<String> sink = Sinks.one();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitValue(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asMono())
          .expectNext("Integer (Mono): 777")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs()).contains("Integer (Mono): 777");
    }
  }

  @Test
  void testFluxLongDeserialization() throws Exception {
    // given
    final String path = "/json/long";
    final Flux<String> data = Flux.just("1234567890", "9876543210");
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asFlux().take(2))
          .expectNext("Long: 1234567890")
          .expectNext("Long: 9876543210")
          .expectComplete()
          .verify();

      assertThat(logCaptor.getInfoLogs()).containsSequence("Long: 1234567890", "Long: 9876543210");
    }
  }

  @Test
  void testFluxBooleanDeserialization() throws Exception {
    // given
    final String path = "/json/boolean";
    final Flux<String> data = Flux.just("true", "false", "true");
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asFlux().take(3))
          .expectNext("Boolean: true")
          .expectNext("Boolean: false")
          .expectNext("Boolean: true")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs())
          .containsSequence("Boolean: true", "Boolean: false", "Boolean: true");
    }
  }

  @Test
  void testFluxPojoDeserialization() throws Exception {
    // given
    final String path = "/json/message/flux";
    final MessageDto msg1 = new MessageDto("Hello", 1000L);
    final MessageDto msg2 = new MessageDto("World", 2000L);

    final Flux<String> data =
        Flux.just(msg1, msg2).map(this::toJson).delayElements(Duration.ofMillis(100));

    final Sinks.Many<String> sink = Sinks.many().replay().all();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asFlux().take(2))
          .expectNext("Message: MessageDto[text=Hello, timestamp=1000]")
          .expectNext("Message: MessageDto[text=World, timestamp=2000]")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs())
          .contains(
              "Message: MessageDto[text=Hello, timestamp=1000]",
              "Message: MessageDto[text=World, timestamp=2000]");
    }
  }

  @Test
  void testMonoPojoDeserialization() throws Exception {
    // given
    final String path = "/json/message/mono";
    final MessageDto msg = new MessageDto("SinglePojo", 3000L);

    final Mono<String> data = Mono.just(msg).map(this::toJson);

    final Sinks.One<String> sink = Sinks.one();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitValue(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asMono())
          .expectNext("Single Message: MessageDto[text=SinglePojo, timestamp=3000]")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs())
          .contains("Single Message: MessageDto[text=SinglePojo, timestamp=3000]");
    }
  }

  @Test
  void testNestedPojoDeserialization() throws Exception {
    // given
    final String path = "/json/user-message";
    final MessageDto innerMsg = new MessageDto("NestedMessage", 4000L);
    final TestUserMessage userMsg = new TestUserMessage("user123", innerMsg);

    final Flux<String> data = Flux.just(userMsg).map(this::toJson);

    final Sinks.Many<String> sink = Sinks.many().replay().all();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asFlux().take(1))
          .expectNext(
              "User Message: TestUserMessage[userId=user123, message=MessageDto[text=NestedMessage, timestamp=4000]]")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs())
          .contains(
              "User Message: TestUserMessage[userId=user123, message=MessageDto[text=NestedMessage, timestamp=4000]]");
    }
  }

  @Test
  void testNestedPojoMonoDeserialization() throws Exception {
    // given
    final String path = "/json/user-message/mono";
    final MessageDto innerMsg = new MessageDto("SingleNested", 5000L);
    final TestUserMessage userMsg = new TestUserMessage("user456", innerMsg);

    final Mono<String> data = Mono.just(userMsg).map(this::toJson);

    final Sinks.One<String> sink = Sinks.one();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitValue(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asMono())
          .expectNext(
              "User Message (Mono): TestUserMessage[userId=user456, message=MessageDto[text=SingleNested, timestamp=5000]]")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs())
          .contains(
              "User Message (Mono): TestUserMessage[userId=user456, message=MessageDto[text=SingleNested, timestamp=5000]]");
    }
  }

  @Test
  void testPojoWithCollectionsDeserialization() throws Exception {
    // given
    final String path = "/json/chat-message";
    final TestChatMessage chatMsg =
        new TestChatMessage("chat001", Arrays.asList("alice", "bob", "charlie"), "Hello everyone!");

    final Flux<String> data = Flux.just(chatMsg).map(this::toJson);

    final Sinks.Many<String> sink = Sinks.many().replay().all();

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(
              path,
              session ->
                  session
                      .send(data.map(session::textMessage))
                      .thenMany(
                          session
                              .receive()
                              .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                      .then())
          .subscribe();

      // verify
      StepVerifier.create(sink.asFlux().take(1))
          .expectNext(
              "Chat Message: TestChatMessage[chatId=chat001, recipients=[alice, bob, charlie], text=Hello everyone!]")
          .expectComplete()
          .verify(DEFAULT_FAST_TEST_FALLBACK);

      assertThat(logCaptor.getInfoLogs())
          .contains(
              "Chat Message: TestChatMessage[chatId=chat001, recipients=[alice, bob, charlie], text=Hello everyone!]");
    }
  }

  @Test
  void testBackwardCompatibilityFlux() throws Exception {
    // given
    final String path = "/json/raw";
    final Flux<String> data = Flux.just("RawMessage1", "RawMessage2");

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(path, session -> session.send(data.map(session::textMessage)).then())
          .subscribe();

      // verify
      Thread.sleep(500);
      assertThat(logCaptor.getInfoLogs()).containsSequence("Raw: RawMessage1", "Raw: RawMessage2");
    }
  }

  @Test
  void testBackwardCompatibilityMono() throws Exception {
    // given
    final String path = "/json/raw/mono";
    final Mono<String> data = Mono.just("SingleRaw");

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(path, session -> session.send(data.map(session::textMessage)).then())
          .subscribe();

      // verify
      Thread.sleep(500);
      assertThat(logCaptor.getInfoLogs()).contains("Raw (Mono): SingleRaw");
    }
  }

  @Test
  void testMultipleMessagesWithFlux() throws Exception {
    // given
    final String path = "/json/message/flux";
    final List<MessageDto> messages =
        List.of(
            new MessageDto("Message1", 1000L),
            new MessageDto("Message2", 2000L),
            new MessageDto("Message3", 3000L),
            new MessageDto("Message4", 4000L),
            new MessageDto("Message5", 5000L));

    final Flux<String> data =
        Flux.fromIterable(messages).map(this::toJson).delayElements(Duration.ofMillis(50));

    try (final LogCaptor logCaptor = LogCaptor.forClass(JsonBodySocketResource.class)) {
      // test
      this.withClient(path, session -> session.send(data.map(session::textMessage)).then())
          .subscribe();

      // verify
      Thread.sleep(1000);
      assertThat(logCaptor.getInfoLogs())
          .contains(
              "Message: MessageDto[text=Message1, timestamp=1000]",
              "Message: MessageDto[text=Message2, timestamp=2000]",
              "Message: MessageDto[text=Message3, timestamp=3000]",
              "Message: MessageDto[text=Message4, timestamp=4000]",
              "Message: MessageDto[text=Message5, timestamp=5000]");
    }
  }

  /** Helper method to convert objects to JSON. */
  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize object to JSON", e);
    }
  }
}
