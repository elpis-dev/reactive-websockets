package io.github.elpis.reactive.websockets.functional.error;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.advice.GlobalWebSocketErrorHandler;
import io.github.elpis.reactive.websockets.context.resource.error.ErrorHandlingResource;
import io.github.elpis.reactive.websockets.functional.BaseWebSocketTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

/**
 * Tests for error handling in WebSocket endpoints. Tests both local and global exception handlers.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({
  BaseWebSocketTest.PermitAllSecurityConfiguration.class,
  ErrorHandlingResource.class,
  GlobalWebSocketErrorHandler.class
})
class ErrorHandlingTest extends BaseWebSocketTest {

  @Test
  void testLocalIOErrorHandler() throws Exception {
    // given
    final String path = "/error-test/io-error";
    final Sinks.One<String> sink = Sinks.one();

    // when
    this.withClient(
            path,
            session -> {
              Flux<String> messages = Flux.just("normal message", "trigger-io", "another message");
              return session
                  .send(messages.map(session::textMessage))
                  .thenMany(
                      session
                          .receive()
                          .map(WebSocketMessage::getPayloadAsText)
                          .doOnNext(sink::tryEmitValue))
                  .then();
            })
        .subscribe();

    StepVerifier.create(sink.asMono().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .assertNext(response -> assertThat(response).contains("Processed: normal message"))
        .verifyComplete();
  }

  @Test
  void testLocalErrorHandler() throws Exception {
    // given
    final String path = "/error-test/local-error";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session -> {
              Flux<String> messages =
                  Flux.just("normal message", "trigger-local", "another message");
              return session
                  .send(messages.map(session::textMessage))
                  .thenMany(
                      session
                          .receive()
                          .map(WebSocketMessage::getPayloadAsText)
                          .doOnNext(sink::tryEmitNext)
                          .take(2))
                  .then();
            })
        .subscribe();

    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK).take(2))
        .assertNext(response -> assertThat(response).contains("Processed: normal message"))
        .assertNext(
            response -> {
              assertThat(response).contains("LOCAL_STATE_ERROR");
              assertThat(response).contains("Local error triggered");
              assertThat(response).contains("\"handled_by\":\"local\"");
            })
        .verifyComplete();
  }

  @Test
  void testGlobalErrorHandler() throws Exception {
    // given
    final String path = "/error-test/global-error";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session -> {
              Flux<String> messages =
                  Flux.just("normal message", "trigger-global", "another message");
              return session
                  .send(messages.map(session::textMessage))
                  .thenMany(
                      session
                          .receive()
                          .map(WebSocketMessage::getPayloadAsText)
                          .doOnNext(sink::tryEmitNext)
                          .take(2))
                  .then();
            })
        .subscribe();

    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK).take(2))
        .assertNext(response -> assertThat(response).contains("Processed: normal message"))
        .assertNext(
            response -> {
              assertThat(response).contains("INVALID_ARGUMENT");
              assertThat(response).contains("Global error triggered");
            })
        .verifyComplete();
  }

  @Test
  void testGlobalRuntimeErrorHandler() throws Exception {
    // given
    final String path = "/error-test/runtime-error";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session -> {
              Flux<String> messages = Flux.just("normal message", "trigger-runtime");
              return session
                  .send(messages.map(session::textMessage))
                  .thenMany(
                      session
                          .receive()
                          .map(WebSocketMessage::getPayloadAsText)
                          .doOnNext(sink::tryEmitNext)
                          .take(2))
                  .then();
            })
        .subscribe();

    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK).take(2))
        .assertNext(response -> assertThat(response).contains("Processed: normal message"))
        .assertNext(
            response -> {
              assertThat(response).contains("INTERNAL_ERROR");
              assertThat(response).contains("An unexpected error occurred");
            })
        .verifyComplete();
  }

  @Test
  void testNoError() throws Exception {
    // given
    final String path = "/error-test/no-error";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session -> {
              Flux<String> messages = Flux.just("hello", "world", "test");
              return session
                  .send(messages.map(session::textMessage))
                  .thenMany(
                      session
                          .receive()
                          .map(WebSocketMessage::getPayloadAsText)
                          .doOnNext(sink::tryEmitNext)
                          .take(3))
                  .then();
            })
        .subscribe();

    // then
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK).take(3))
        .expectNext("Echo: hello")
        .expectNext("Echo: world")
        .expectNext("Echo: test")
        .verifyComplete();
  }

  @Test
  void testLocalHandlerPriorityOverGlobal() throws Exception {
    // Test that local handler takes precedence over global handler
    // Both handlers could handle IllegalStateException (as RuntimeException parent)
    // but local should be called first
    final String path = "/error-test/local-error";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session -> {
              Flux<String> messages = Flux.just("trigger-local");
              return session
                  .send(messages.map(session::textMessage))
                  .thenMany(
                      session
                          .receive()
                          .map(WebSocketMessage::getPayloadAsText)
                          .doOnNext(sink::tryEmitNext)
                          .take(1))
                  .then();
            })
        .subscribe();

    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK).take(1))
        .assertNext(
            response -> {
              assertThat(response).contains("LOCAL_STATE_ERROR");
              assertThat(response).contains("\"handled_by\":\"local\"");
            })
        .verifyComplete();
  }
}
