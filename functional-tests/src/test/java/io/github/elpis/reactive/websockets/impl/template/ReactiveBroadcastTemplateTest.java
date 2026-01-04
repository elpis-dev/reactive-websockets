package io.github.elpis.reactive.websockets.impl.template;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.controller.BroadcastController;
import io.github.elpis.reactive.websockets.context.resource.template.BroadcastReceiverResource;
import io.github.elpis.reactive.websockets.template.ReactiveWebSocketTemplate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

/**
 * Functional tests for {@link ReactiveWebSocketTemplate}.
 *
 * <p>These tests verify the template's ability to send messages to WebSocket sessions, including
 * broadcast, targeted, and error scenarios.
 *
 * <p>Tests use multiple concurrent WebSocket clients to simulate real-world usage patterns.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({
  BaseWebSocketTest.PermitAllSecurityConfiguration.class,
  BroadcastController.class,
  BroadcastReceiverResource.class
})
class ReactiveBroadcastTemplateTest extends BaseWebSocketTest {
  private final List<String> history =
      List.of(
          "Bob: Hi Alice!",
          "Alice: Hey Bob, how was your trip?",
          "Bob: Awful, the plane was delayed for 4 hours");

  @Test
  void testSendBroadcastSinglePayloadAllSessionsReceive() throws Exception {
    final String path = "/template/broadcast/single";

    final Sinks.Many<String> alicesSink = Sinks.many().replay().all();
    final Sinks.Many<String> bobsSink = Sinks.many().replay().all();
    final CountDownLatch latch = new CountDownLatch(2);

    // Alice connects
    withClient(
            path,
            session ->
                session
                    .receive()
                    .doOnSubscribe(subscription -> latch.countDown())
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(alicesSink::tryEmitNext)
                    .then())
        .doOnSubscribe(subscription -> latch.countDown())
        .subscribe();

    // Bob connects
    withClient(
            path,
            session ->
                session
                    .receive()
                    .doOnSubscribe(subscription -> latch.countDown())
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(bobsSink::tryEmitNext)
                    .then())
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    this.getWebClient()
        .post()
        .uri("/broadcast")
        .bodyValue("All users connected")
        .exchange()
        .expectStatus()
        .isNoContent();

    StepVerifier.create(alicesSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("All users connected")
        .verifyError(TimeoutException.class);

    StepVerifier.create(bobsSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("All users connected")
        .verifyError(TimeoutException.class);
  }

  @Test
  void testSendBroadcastStreamPayloadAllSessionsReceive() throws Exception {
    final String path = "/template/broadcast/many";

    final Sinks.Many<String> alicesSink = Sinks.many().replay().all();
    final Sinks.Many<String> bobsSink = Sinks.many().replay().all();

    final CountDownLatch latch = new CountDownLatch(2);

    // Alice connects
    withClient(
            path,
            session ->
                session
                    .receive()
                    .doOnSubscribe(subscription -> latch.countDown())
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(alicesSink::tryEmitNext)
                    .then())
        .subscribe();

    // Bob connects
    withClient(
            path,
            session ->
                session
                    .receive()
                    .doOnSubscribe(subscription -> latch.countDown())
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(bobsSink::tryEmitNext)
                    .then())
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    this.getWebClient()
        .post()
        .uri("/broadcast/many")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(history)
        .exchange()
        .expectStatus()
        .isNoContent();

    StepVerifier.create(alicesSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Bob: Hi Alice!")
        .expectNext("Alice: Hey Bob, how was your trip?")
        .expectNext("Bob: Awful, the plane was delayed for 4 hours")
        .verifyError(TimeoutException.class);

    StepVerifier.create(bobsSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Bob: Hi Alice!")
        .expectNext("Alice: Hey Bob, how was your trip?")
        .expectNext("Bob: Awful, the plane was delayed for 4 hours")
        .verifyError(TimeoutException.class);
  }
}
