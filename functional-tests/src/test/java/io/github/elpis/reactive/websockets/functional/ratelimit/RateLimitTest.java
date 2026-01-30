package io.github.elpis.reactive.websockets.functional.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.flowcontrol.RateLimitResource;
import io.github.elpis.reactive.websockets.functional.BaseWebSocketTest;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
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
    classes = {BootStarter.class})
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, RateLimitResource.class})
public class RateLimitTest extends BaseWebSocketTest {

  // Test constants for rate limits
  private static final int NO_RATE_LIMIT_MESSAGE_COUNT = 20;

  // Test message counts
  private static final int DEFAULT_TEST_MESSAGE_COUNT = 10;
  private static final int CUSTOM_TEST_MESSAGE_COUNT = 12;
  private static final int GENERIC_TEST_MESSAGE_COUNT = 5;

  private static final String SUCCESSFUL_MESSAGE = "Message Processed";
  private static final String RATE_LIMIT_MESSAGE = "Rate Limit hit in: ";

  @Test
  public void testDefaultRateLimitInherited() throws Exception {
    // given
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(150))
            .map(i -> RATE_LIMIT_MESSAGE + (GENERIC_TEST_MESSAGE_COUNT - i))
            .take(DEFAULT_TEST_MESSAGE_COUNT);
    final String[] messagesToExpect =
        Stream.generate(() -> SUCCESSFUL_MESSAGE)
            .limit(GENERIC_TEST_MESSAGE_COUNT)
            .toArray(String[]::new);

    final String path = "/ratelimit/default";
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
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(sink::tryEmitNext))
                    .then(Mono.never()))
        .subscribe();

    // verify
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_GENERIC_TEST_FALLBACK))
        .expectNext(messagesToExpect)
        .verifyError(TimeoutException.class);
  }

  @Test
  public void testCustomRateLimitOverride() throws Exception {
    // given
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(50))
            .map(i -> "Rate Limit hit in: " + (GENERIC_TEST_MESSAGE_COUNT - i))
            .take(CUSTOM_TEST_MESSAGE_COUNT);
    final String[] messagesToExpect =
        Stream.generate(() -> SUCCESSFUL_MESSAGE)
            .limit(GENERIC_TEST_MESSAGE_COUNT)
            .toArray(String[]::new);

    final String path = "/ratelimit/custom";
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
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(sink::tryEmitNext))
                    .then(Mono.never()))
        .subscribe();

    // verify
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_GENERIC_TEST_FALLBACK))
        .expectNext(messagesToExpect)
        .verifyError(TimeoutException.class);
  }

  @Test
  public void testDisabledRateLimit() throws Exception {
    // given
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(10))
            .map(i -> "No Rate Limit")
            .take(NO_RATE_LIMIT_MESSAGE_COUNT);

    final String path = "/ratelimit/disabled";
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
                            .map(WebSocketMessage::getPayloadAsText)
                            .filter(message -> !SUCCESSFUL_MESSAGE.equals(message))
                            .doOnNext(sink::tryEmitNext))
                    .then(Mono.never()))
        .subscribe();

    // verify
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }

  @Test
  public void testUserScopedRateLimit() throws Exception {
    // given
    final int allowedMessageCount = 3;
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(150))
            .map(i -> "Rate Limit hit in: " + (allowedMessageCount - i))
            .take(CUSTOM_TEST_MESSAGE_COUNT);
    final String[] messagesToExpect =
        Stream.generate(() -> SUCCESSFUL_MESSAGE).limit(allowedMessageCount).toArray(String[]::new);

    final String path = "/ratelimit/by-user";
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
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(sink::tryEmitNext))
                    .then(Mono.never()))
        .subscribe();

    // verify
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_GENERIC_TEST_FALLBACK))
        .expectNext(messagesToExpect)
        .verifyError(TimeoutException.class);
  }

  /**
   * Tests IP-scoped rate limiting which uses getRateLimiterIdentifier with scope = IP. The rate
   * limiter identifier should be based on the client's IP address. Multiple connections from the
   * same IP share the same rate limit.
   */
  @Test
  public void testIpScopedRateLimit() throws Exception {
    // given
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(150))
            .map(i -> "Rate Limit hit in: " + (GENERIC_TEST_MESSAGE_COUNT - i))
            .take(8);
    final String[] messagesToExpect =
        Stream.generate(() -> SUCCESSFUL_MESSAGE)
            .limit(GENERIC_TEST_MESSAGE_COUNT)
            .toArray(String[]::new);

    final String path = "/ratelimit/by-ip";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // test - connect from localhost, excess messages should be dropped
    this.withClient(
            path,
            session ->
                session
                    .send(data.map(session::textMessage))
                    .thenMany(
                        session
                            .receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(sink::tryEmitNext))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_GENERIC_TEST_FALLBACK))
        .expectNext(messagesToExpect)
        .verifyError(TimeoutException.class);
  }

  /**
   * Tests that multiple connections from the same IP share the same rate limit. This verifies that
   * getRateLimiterIdentifier correctly generates the same identifier for connections from the same
   * IP address.
   */
  @Test
  public void testIpScopedRateLimitSharedAcrossConnections() throws Exception {
    // given
    final Flux<String> data1 =
        Flux.interval(Duration.ofMillis(150)).map(i -> "Connection 1, Message: " + (i + 1)).take(3);
    final Flux<String> data2 =
        Flux.interval(Duration.ofMillis(150)).map(i -> "Connection 2, Message: " + (i + 1)).take(4);
    final String[] messagesToExpect =
        Stream.generate(() -> SUCCESSFUL_MESSAGE)
            .limit(GENERIC_TEST_MESSAGE_COUNT)
            .toArray(String[]::new);

    final String path = "/ratelimit/by-ip";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    final CountDownLatch latch = new CountDownLatch(2);

    // First connection - send 3 messages (within limit)
    this.withClient(
            path,
            session ->
                session
                    .send(data1.map(session::textMessage))
                    .thenMany(
                        session
                            .receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(sink::tryEmitNext))
                    .doOnSubscribe(__ -> latch.countDown())
                    .then(Mono.never()))
        .subscribe();

    this.withClient(
            path,
            session ->
                session
                    .send(data2.map(session::textMessage))
                    .thenMany(
                        session
                            .receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(sink::tryEmitNext))
                    .doOnSubscribe(__ -> latch.countDown())
                    .then(Mono.never()))
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    // verify
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_GENERIC_TEST_FALLBACK))
        .expectNext(messagesToExpect)
        .verifyError(TimeoutException.class);
  }
}
