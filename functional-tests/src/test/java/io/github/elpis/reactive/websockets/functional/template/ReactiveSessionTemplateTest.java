package io.github.elpis.reactive.websockets.functional.template;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.template.SessionReceiverResource;
import io.github.elpis.reactive.websockets.functional.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.template.ReactiveWebSocketTemplate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, SessionReceiverResource.class})
class ReactiveSessionTemplateTest extends BaseWebSocketTest {

  @Test
  void testSendToSessionSinglePayloadSessionReceives() throws Exception {
    final String path = "/template/session/single";

    final Sinks.Many<String> alicesSink = Sinks.many().replay().all();
    final Sinks.Many<String> bobsSink = Sinks.many().replay().all();

    final CountDownLatch latch = new CountDownLatch(2);

    // Alice connects
    withClient(
            path + "?userName=Alice",
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
            path + "?userName=Bob",
            session ->
                session
                    .receive()
                    .doOnSubscribe(subscription -> latch.countDown())
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(bobsSink::tryEmitNext)
                    .then())
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    StepVerifier.create(alicesSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Alice connected")
        .verifyError(TimeoutException.class);

    StepVerifier.create(bobsSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Bob connected")
        .verifyError(TimeoutException.class);
  }

  @Test
  void testSendToSessionStreamPayloadSessionReceives() throws Exception {
    final String path = "/template/session/many";

    final Sinks.Many<String> alicesSink = Sinks.many().replay().all();
    final Sinks.Many<String> bobsSink = Sinks.many().replay().all();

    final CountDownLatch latch = new CountDownLatch(2);

    // Alice connects
    withClient(
            path + "?userName=Alice",
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(alicesSink::tryEmitNext)
                    .then())
        .doOnSubscribe(subscription -> latch.countDown())
        .subscribe();

    // Bob connects
    withClient(
            path + "?userName=Bob",
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(bobsSink::tryEmitNext)
                    .then())
        .doOnSubscribe(subscription -> latch.countDown())
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    StepVerifier.create(alicesSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Alice is good")
        .expectNext("Alice is awesome")
        .verifyError(TimeoutException.class);

    StepVerifier.create(bobsSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Bob is good")
        .expectNext("Bob is awesome")
        .verifyError(TimeoutException.class);
  }

  @Test
  void testSendToMultipleSessionsSinglePayloadAllSessionsReceive() throws Exception {
    final String path = "/template/session/multi/single";

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

    this.getWebClient().post().uri("/session/many").exchange().expectStatus().isNoContent();

    StepVerifier.create(alicesSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Please be informed about the following updates.")
        .verifyError(TimeoutException.class);

    StepVerifier.create(bobsSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Please be informed about the following updates.")
        .verifyError(TimeoutException.class);
  }

  @Test
  void testSendToMultipleSessionsStreamPayloadAllSessionsReceive() throws Exception {
    final String path = "/template/session/multi/many";

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

    this.getWebClient().post().uri("/session/many/stream").exchange().expectStatus().isNoContent();

    StepVerifier.create(alicesSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Maintenance Notice: The system will be down for maintenance at midnight.")
        .expectNext("Update: New features have been added to your dashboard.")
        .verifyError(TimeoutException.class);

    StepVerifier.create(bobsSink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Maintenance Notice: The system will be down for maintenance at midnight.")
        .expectNext("Update: New features have been added to your dashboard.")
        .verifyError(TimeoutException.class);
  }
}
