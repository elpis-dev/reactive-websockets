package io.github.elpis.reactive.websockets.impl.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.flowcontrol.RateLimitResource;
import io.github.elpis.reactive.websockets.handler.AdaptiveWebSocketHandler;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
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
  private static final int USER_TEST_MESSAGE_COUNT = 6;

  @Test
  public void testDefaultRateLimitInherited() throws Exception {
    // given
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(150))
            .map(i -> "Till Rate Limit is hit: " + (i + 1))
            .take(DEFAULT_TEST_MESSAGE_COUNT);

    final String path = "/ratelimit/default";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    final LogCaptor logCaptor = LogCaptor.forClass(AdaptiveWebSocketHandler.class);

    // test - send messages, rate limiter should drop excess messages
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

    // verify - wait for all messages to be sent
    Thread.sleep(2000);

    // Rate limit is 5 per 10 seconds (default), so we should only receive 5 messages, rest are
    // dropped
    assertThat(logCaptor.getWarnLogs())
        .anySatisfy(log -> assertThat(log).contains("Rate limit exceeded"));
  }

  @Test
  public void testCustomRateLimitOverride() throws Exception {
    // given
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(50))
            .map(i -> "Till Rate Limit is hit: " + (i + 1))
            .take(CUSTOM_TEST_MESSAGE_COUNT);

    final String path = "/ratelimit/custom";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    final LogCaptor logCaptor = LogCaptor.forClass(AdaptiveWebSocketHandler.class);

    // test - send messages, rate limiter should drop excess messages
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

    // verify - wait for all messages to be sent
    Thread.sleep(1500);

    // Rate limit is 10 per 10 seconds (custom), so we should drop excess messages
    assertThat(logCaptor.getWarnLogs())
        .anySatisfy(log -> assertThat(log).contains("Rate limit exceeded"));
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

    final LogCaptor logCaptor = LogCaptor.forClass(AdaptiveWebSocketHandler.class);

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
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);

    assertThat(logCaptor.getWarnLogs()).noneMatch(log -> log.contains("Rate limit exceeded"));
  }

  @Test
  public void testUserScopedRateLimit() throws Exception {
    // given
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(150))
            .map(i -> "Till Rate Limit is hit: " + (i + 1))
            .take(USER_TEST_MESSAGE_COUNT);

    final String path = "/ratelimit/by-user";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    final LogCaptor logCaptor = LogCaptor.forClass(AdaptiveWebSocketHandler.class);

    // test - send messages, rate limiter should drop excess messages
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

    // verify - wait for all messages to be sent
    Thread.sleep(1500);

    // Rate limit is 3 per 10 seconds (user scope), excess messages should be dropped
    assertThat(logCaptor.getWarnLogs())
        .anySatisfy(log -> assertThat(log).contains("Rate limit exceeded"));
  }

  /**
   * Tests IP-scoped rate limiting which uses getRateLimiterIdentifier with scope = IP. The rate
   * limiter identifier should be based on the client's IP address. Multiple connections from the
   * same IP share the same rate limit.
   */
  @Test
  public void testIpScopedRateLimit() throws Exception {
    // given - IP scope allows 5 messages per 10 seconds
    final Flux<String> data =
        Flux.interval(Duration.ofMillis(150))
            .map(i -> "Message from IP: " + (i + 1))
            .take(8); // Send 8 messages to exceed the limit of 5

    final String path = "/ratelimit/by-ip";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    final LogCaptor logCaptor = LogCaptor.forClass(AdaptiveWebSocketHandler.class);

    // test - connect from localhost (127.0.0.1 or similar), excess messages should be dropped
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

    // verify - wait for all messages to be sent
    Thread.sleep(2000);

    // verify rate limit warning was logged (3 messages dropped)
    assertThat(logCaptor.getWarnLogs())
        .anySatisfy(log -> assertThat(log).contains("Rate limit exceeded"));
  }

  /**
   * Tests that multiple connections from the same IP share the same rate limit. This verifies that
   * getRateLimiterIdentifier correctly generates the same identifier for connections from the same
   * IP address.
   */
  @Test
  public void testIpScopedRateLimitSharedAcrossConnections() throws Exception {
    // given - IP scope allows 5 messages per 10 seconds
    final String path = "/ratelimit/by-ip";
    final Sinks.Many<String> sink1 = Sinks.many().replay().all();
    final Sinks.Many<String> sink2 = Sinks.many().replay().all();

    final LogCaptor logCaptor = LogCaptor.forClass(AdaptiveWebSocketHandler.class);

    // First connection - send 3 messages (within limit)
    final Flux<String> data1 =
        Flux.interval(Duration.ofMillis(150)).map(i -> "Connection 1, Message: " + (i + 1)).take(3);

    this.withClient(
            path,
            session ->
                session
                    .send(data1.map(session::textMessage))
                    .thenMany(
                        session
                            .receive()
                            .doOnNext(value -> sink1.tryEmitNext(value.getPayloadAsText())))
                    .then())
        .subscribe();

    // Allow first connection to process
    Thread.sleep(800);

    // Second connection from same IP - send 4 more messages (should exceed shared limit of 5)
    final Flux<String> data2 =
        Flux.interval(Duration.ofMillis(150)).map(i -> "Connection 2, Message: " + (i + 1)).take(4);

    this.withClient(
            path,
            session ->
                session
                    .send(data2.map(session::textMessage))
                    .thenMany(
                        session
                            .receive()
                            .doOnNext(value -> sink2.tryEmitNext(value.getPayloadAsText())))
                    .then())
        .subscribe();

    // verify - give time for second connection to exceed rate limit (3 + 4 = 7, exceeds 5, so 2
    // messages dropped)
    Thread.sleep(1500);

    // verify rate limit warning was logged
    assertThat(logCaptor.getWarnLogs())
        .anySatisfy(log -> assertThat(log).contains("Rate limit exceeded"));
  }
}
