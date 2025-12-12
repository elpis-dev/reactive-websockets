package io.github.elpis.reactive.websockets.impl.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.flowcontrol.RateLimitResource;
import io.github.elpis.reactive.websockets.handler.BroadcastWebSocketResourceHandler;
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

    final LogCaptor logCaptor = LogCaptor.forClass(BroadcastWebSocketResourceHandler.class);

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

    final LogCaptor logCaptor = LogCaptor.forClass(BroadcastWebSocketResourceHandler.class);

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

    final LogCaptor logCaptor = LogCaptor.forClass(BroadcastWebSocketResourceHandler.class);

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

    final LogCaptor logCaptor = LogCaptor.forClass(BroadcastWebSocketResourceHandler.class);

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

    assertThat(logCaptor.getWarnLogs())
        .anySatisfy(log -> assertThat(log).contains("Rate limit exceeded"));
  }
}
