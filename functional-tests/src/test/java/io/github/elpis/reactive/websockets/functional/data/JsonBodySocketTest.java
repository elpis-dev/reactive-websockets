package io.github.elpis.reactive.websockets.functional.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.model.TestChatMessage;
import io.github.elpis.reactive.websockets.context.model.TestMessage;
import io.github.elpis.reactive.websockets.context.model.TestUserMessage;
import io.github.elpis.reactive.websockets.context.model.ValidatedMessage;
import io.github.elpis.reactive.websockets.context.resource.data.JsonBodySocketResource;
import io.github.elpis.reactive.websockets.functional.BaseWebSocketTest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
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
  }

  @Test
  void testMonoStringDeserialization() throws Exception {
    // given
    final String path = "/json/string/mono";
    final Mono<String> data = Mono.just("SingleMessage");
    final Sinks.One<String> sink = Sinks.one();

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
  }

  @Test
  void testFluxIntegerDeserialization() throws Exception {
    // given
    final String path = "/json/integer";
    final Flux<String> data = Flux.just("42", "100", "999");
    final Sinks.Many<String> sink = Sinks.many().replay().all();

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
  }

  @Test
  void testMonoIntegerDeserialization() throws Exception {
    // given
    final String path = "/json/integer/mono";
    final Mono<String> data = Mono.just("777");
    final Sinks.One<String> sink = Sinks.one();

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
  }

  @Test
  void testFluxLongDeserialization() throws Exception {
    // given
    final String path = "/json/long";
    final Flux<String> data = Flux.just("1234567890", "9876543210");
    final Sinks.Many<String> sink = Sinks.many().replay().all();

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
  }

  @Test
  void testFluxBooleanDeserialization() throws Exception {
    // given
    final String path = "/json/boolean";
    final Flux<String> data = Flux.just("true", "false", "true");
    final Sinks.Many<String> sink = Sinks.many().replay().all();

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
  }

  @Test
  void testFluxPojoDeserialization() throws Exception {
    // given
    final String path = "/json/message/flux";
    final TestMessage msg1 = new TestMessage("Hello", 1000L);
    final TestMessage msg2 = new TestMessage("World", 2000L);

    final Flux<String> data =
        Flux.just(msg1, msg2).map(this::toJson).delayElements(Duration.ofMillis(100));

    final Sinks.Many<String> sink = Sinks.many().replay().all();

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
        .expectNext("Message: TestMessage[text=Hello, timestamp=1000]")
        .expectNext("Message: TestMessage[text=World, timestamp=2000]")
        .expectComplete()
        .verify(DEFAULT_FAST_TEST_FALLBACK);
  }

  @Test
  void testMonoPojoDeserialization() throws Exception {
    // given
    final String path = "/json/message/mono";
    final TestMessage msg = new TestMessage("SinglePojo", 3000L);

    final Mono<String> data = Mono.just(msg).map(this::toJson);

    final Sinks.One<String> sink = Sinks.one();

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
        .expectNext("Single Message: TestMessage[text=SinglePojo, timestamp=3000]")
        .expectComplete()
        .verify(DEFAULT_FAST_TEST_FALLBACK);
  }

  @Test
  void testNestedPojoDeserialization() throws Exception {
    // given
    final String path = "/json/user-message";
    final TestMessage innerMsg = new TestMessage("NestedMessage", 4000L);
    final TestUserMessage userMsg = new TestUserMessage("user123", innerMsg);

    final Flux<String> data = Flux.just(userMsg).map(this::toJson);

    final Sinks.Many<String> sink = Sinks.many().replay().all();

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
            "User Message: TestUserMessage[userId=user123, message=TestMessage[text=NestedMessage, timestamp=4000]]")
        .expectComplete()
        .verify(DEFAULT_FAST_TEST_FALLBACK);
  }

  @Test
  void testNestedPojoMonoDeserialization() throws Exception {
    // given
    final String path = "/json/user-message/mono";
    final TestMessage innerMsg = new TestMessage("SingleNested", 5000L);
    final TestUserMessage userMsg = new TestUserMessage("user456", innerMsg);

    final Mono<String> data = Mono.just(userMsg).map(this::toJson);

    final Sinks.One<String> sink = Sinks.one();

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
            "User Message (Mono): TestUserMessage[userId=user456, message=TestMessage[text=SingleNested, timestamp=5000]]")
        .expectComplete()
        .verify(DEFAULT_FAST_TEST_FALLBACK);
  }

  @Test
  void testPojoWithCollectionsDeserialization() throws Exception {
    // given
    final String path = "/json/chat-message";
    final TestChatMessage chatMsg =
        new TestChatMessage("chat001", Arrays.asList("alice", "bob", "charlie"), "Hello everyone!");

    final Flux<String> data = Flux.just(chatMsg).map(this::toJson);

    final Sinks.Many<String> sink = Sinks.many().replay().all();

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
  }

  @Test
  void testBackwardCompatibilityFlux() throws Exception {
    // given
    final String path = "/json/raw";
    final Flux<String> data = Flux.just("RawMessage1", "RawMessage2");

    // test
    this.withClient(path, session -> session.send(data.map(session::textMessage)).then())
        .subscribe();

    // verify
    Thread.sleep(500);
  }

  @Test
  void testBackwardCompatibilityMono() throws Exception {
    // given
    final String path = "/json/raw/mono";
    final Mono<String> data = Mono.just("SingleRaw");

    // test
    this.withClient(path, session -> session.send(data.map(session::textMessage)).then())
        .subscribe();

    // verify
    Thread.sleep(500);
  }

  @Test
  void testMultipleMessagesWithFlux() throws Exception {
    // given
    final String path = "/json/message/flux";
    final List<TestMessage> messages =
        List.of(
            new TestMessage("Message1", 1000L),
            new TestMessage("Message2", 2000L),
            new TestMessage("Message3", 3000L),
            new TestMessage("Message4", 4000L),
            new TestMessage("Message5", 5000L));

    final Flux<String> data =
        Flux.fromIterable(messages).map(this::toJson).delayElements(Duration.ofMillis(50));

    // test
    this.withClient(path, session -> session.send(data.map(session::textMessage)).then())
        .subscribe();

    // verify
    Thread.sleep(1000);
  }

  @Test
  void testPojoWithValidation() throws Exception {
    // given
    final String path = "/json/validated";
    final ValidatedMessage validatedMessage = new ValidatedMessage("definitelyNotEmail");

    final Mono<String> data = Mono.just(validatedMessage).map(this::toJson);

    final Sinks.One<String> sink = Sinks.one();

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
        .assertNext(
            value ->
                assertThat(value)
                    .contains("username: must be a well-formed email address")
                    .contains("VALIDATION_FAILED"))
        .expectComplete()
        .verify(DEFAULT_FAST_TEST_FALLBACK);
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
