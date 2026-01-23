package io.github.elpis.reactive.websockets.impl.data;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.data.MessageBodySocketResource;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, MessageBodySocketResource.class})
class BodySocketTest extends BaseWebSocketTest {

  @Test
  void receiveDefaultMessageTestLong() throws Exception {
    // given
    final Flux<String> data = Flux.interval(Duration.ofMillis(100)).map(i -> "Entry " + i).take(50);

    final String path = "/body/post";
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

    // verify - endpoint receives messages but doesn't respond, so timeout is expected
    StepVerifier.create(
            sink.asFlux().timeout(DEFAULT_GENERIC_TEST_FALLBACK.plus(DEFAULT_FAST_TEST_FALLBACK)))
        .verifyError(TimeoutException.class);
  }

  @Test
  void postBinaryMessage() throws Exception {
    // given
    final String path = "/body/post/binary";
    final Sinks.One<String> sink = Sinks.one();

    // test
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .filter(
                        webSocketMessage ->
                            webSocketMessage.getType() == WebSocketMessage.Type.BINARY)
                    .doOnNext(
                        webSocketMessage -> sink.tryEmitValue(webSocketMessage.getPayloadAsText()))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext("Binary")
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void postStreamMessage() throws Exception {
    // given
    final String path = "/body/post/stream";
    final Sinks.One<String> sink = Sinks.one();

    // test
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .filter(
                        webSocketMessage ->
                            webSocketMessage.getType() == WebSocketMessage.Type.BINARY)
                    .doOnNext(
                        webSocketMessage -> sink.tryEmitValue(webSocketMessage.getPayloadAsText()))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext("Stream")
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void receiveErrorTest() throws Exception {
    // given
    final String path = "/body/post";
    final Sinks.Many<String> sink = Sinks.many().replay().all();
    final Sinks.One<String> errorSink = Sinks.one();

    final String data = this.randomTextString(10);

    // test
    this.withClient(
            path,
            session ->
                session
                    .send(Mono.error(new RuntimeException(data)))
                    .thenMany(
                        session
                            .receive()
                            .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                    .then())
        .doOnError(throwable -> errorSink.tryEmitValue(throwable.getMessage()))
        .subscribe();

    // verify - error should be caught and no messages received
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);

    StepVerifier.create(errorSink.asMono()).expectNext(data).expectComplete().verify();
  }

  @Test
  void receiveStringFluxTest() throws Exception {
    // given
    final String path = "/body/post/string";
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

    // verify - endpoint receives messages but doesn't respond
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }

  @Test
  void receiveStringMonoTest() throws Exception {
    // given
    final String path = "/body/post/string/mono";
    final Mono<String> data = Mono.just("SingleMessage");
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

    // verify - endpoint receives messages but doesn't respond
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }

  @Test
  void receiveIntegerFluxTest() throws Exception {
    // given
    final String path = "/body/post/integer";
    final Flux<Integer> data = Flux.just(42, 100, 999);
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // test
    this.withClient(
            path,
            session ->
                session
                    .send(data.map(val -> session.textMessage(val.toString())))
                    .thenMany(
                        session
                            .receive()
                            .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                    .then())
        .subscribe();

    // verify - endpoint receives messages but doesn't respond
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }

  @Test
  void receiveIntegerMonoTest() throws Exception {
    // given
    final String path = "/body/post/integer/mono";
    final Mono<Integer> data = Mono.just(777);
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // test
    this.withClient(
            path,
            session ->
                session
                    .send(data.map(val -> session.textMessage(val.toString())))
                    .thenMany(
                        session
                            .receive()
                            .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                    .then())
        .subscribe();

    // verify - endpoint receives messages but doesn't respond
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }

  @Test
  void receiveLongFluxTest() throws Exception {
    // given
    final String path = "/body/post/long";
    final Flux<Long> data = Flux.just(1234567890L, 9876543210L);
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // test
    this.withClient(
            path,
            session ->
                session
                    .send(data.map(val -> session.textMessage(val.toString())))
                    .thenMany(
                        session
                            .receive()
                            .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                    .then())
        .subscribe();

    // verify - endpoint receives messages but doesn't respond
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }

  @Test
  void receiveBooleanFluxTest() throws Exception {
    // given
    final String path = "/body/post/boolean";
    final Flux<Boolean> data = Flux.just(true, false, true);
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // test
    this.withClient(
            path,
            session ->
                session
                    .send(data.map(val -> session.textMessage(val.toString())))
                    .thenMany(
                        session
                            .receive()
                            .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                    .then())
        .subscribe();

    // verify - endpoint receives messages but doesn't respond
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }
}
