package io.github.elpis.reactive.websockets.handler.flowcontrol.impl;

import static io.github.elpis.reactive.websockets.Constants.DEFAULT_KEY;

import io.github.elpis.reactive.websockets.exception.flowcontrol.RateLimitWarningException;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPlacement;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPolicy;
import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveRateLimitFlowControlRegistry;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Flow control policy that applies rate limiting to inbound WebSocket message streams.
 *
 * <p>This policy checks for rate limit configuration in the {@link
 * ReactiveRateLimitFlowControlRegistry} based on the WebSocket path. If a configuration is found
 * and enabled, it applies the specified rate limiting to the incoming message Flux.
 *
 * <p>Rate limiting is enforced using Resilience4j's RateLimiter.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class ReactiveRateLimitFlowControlPolicy implements FlowControlPolicy {
  private static final Logger log =
      LoggerFactory.getLogger(ReactiveRateLimitFlowControlPolicy.class);

  private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
  private final Map<String, Instant> lastWarningTimestamps = new ConcurrentHashMap<>();

  private final ReactiveRateLimitFlowControlRegistry reactiveRateLimitFlowControlRegistry;
  private final ReactiveWebSocketSessionRegistry sessionRegistry;

  public ReactiveRateLimitFlowControlPolicy(
      final ReactiveRateLimitFlowControlRegistry reactiveRateLimitFlowControlRegistry,
      final ReactiveWebSocketSessionRegistry sessionRegistry) {

    this.reactiveRateLimitFlowControlRegistry = reactiveRateLimitFlowControlRegistry;
    this.sessionRegistry = sessionRegistry;
  }

  /**
   * Applies rate limiting to the incoming WebSocket message Flux based on the path.
   *
   * @param path the WebSocket endpoint path
   * @param context the WebSocket session context
   * @param webSocketMessageFlux the original Flux of WebSocket messages
   * @return the Flux with rate limiting applied if configured, otherwise the original Flux
   */
  @Override
  public Flux<WebSocketMessage> apply(
      final String path,
      final WebSocketSessionContext context,
      final Flux<WebSocketMessage> webSocketMessageFlux) {
    final Optional<RateLimitConfig> rateLimitConfig =
        Optional.ofNullable(reactiveRateLimitFlowControlRegistry.get(path))
            .or(() -> Optional.ofNullable(reactiveRateLimitFlowControlRegistry.get(DEFAULT_KEY)))
            .filter(RateLimitConfig::isEnabled);

    return rateLimitConfig
        .map(
            config -> {
              final String identifier = this.resolveRateLimitIdentifier(config.getScope(), context);
              final RateLimiter rateLimiter =
                  this.getOrCreateRateLimiter(path + ":" + identifier, config);
              return this.applyRateLimit(
                  rateLimiter, webSocketMessageFlux, identifier, config, path, context);
            })
        .orElse(webSocketMessageFlux);
  }

  /**
   * Specifies that this flow control policy applies to the input (incoming messages).
   *
   * @return FlowControlPlacement.INPUT
   */
  @Override
  public FlowControlPlacement placement() {
    return FlowControlPlacement.INPUT;
  }

  private Flux<WebSocketMessage> applyRateLimit(
      final RateLimiter rateLimiter,
      final Flux<WebSocketMessage> webSocketMessageFlux,
      final String identifier,
      final RateLimitConfig config,
      final String path,
      final WebSocketSessionContext context) {
    if (!config.isEnabled()) {
      return webSocketMessageFlux;
    }

    return webSocketMessageFlux.concatMap(
        webSocketMessage -> {
          final boolean permitted = rateLimiter.acquirePermission();
          if (permitted) {
            int remaining = rateLimiter.getMetrics().getAvailablePermissions();
            int total = rateLimiter.getRateLimiterConfig().getLimitForPeriod();
            double utilization = 1.0 - ((double) remaining / total);

            if (log.isTraceEnabled()) {
              log.trace(
                  "Rate limit check passed for identifier: {}. Remaining: {}/{}. Utilization: {}%",
                  identifier, remaining, total, String.format("%.2f", utilization * 100));
            }

            final Optional<Double> warningThresholdOpt = config.getWarningThreshold();
            if (warningThresholdOpt.isPresent() && utilization >= warningThresholdOpt.get()) {
              final Double warningThreshold = warningThresholdOpt.get();
              if (log.isWarnEnabled()) {
                log.warn(
                    "Rate limit utilization warning for identifier: {}. Utilization: {}% exceeds threshold of {}%",
                    identifier,
                    String.format("%.2f", utilization * 100),
                    String.format("%.2f", warningThreshold * 100));
              }

              if (this.shouldSendWarning(identifier, config)) {
                this.sessionRegistry
                    .getSession(path, context.sessionId())
                    .ifPresent(
                        session ->
                            session
                                .outboundSink()
                                .tryEmitError(
                                    new RateLimitWarningException(
                                        "Rate limit utilization warning",
                                        remaining,
                                        (int) (utilization * 100))));
                this.lastWarningTimestamps.put(identifier, Instant.now());
              }
            }

            return Mono.just(webSocketMessage);
          } else {
            if (log.isWarnEnabled()) {
              log.warn("Rate limit exceeded for identifier: {}", identifier);
            }

            return Mono.empty();
          }
        });
  }

  private String resolveRateLimitIdentifier(
      final RateLimit.RateLimitScope scope, final WebSocketSessionContext context) {
    return switch (scope) {
      case USER -> {
        final Object principal = context.getPrincipal("", false, Object.class);
        yield principal != null ? principal.toString() : context.sessionId();
      }
      case IP -> {
        final String remoteAddress = context.remoteAddress();
        yield remoteAddress != null ? remoteAddress : context.sessionId();
      }
      default -> context.sessionId();
    };
  }

  private RateLimiter getOrCreateRateLimiter(final String key, final RateLimitConfig config) {
    final RateLimiterConfig rateLimiterConfig =
        RateLimiterConfig.custom()
            .limitRefreshPeriod(
                Duration.ofMillis(config.getTimeUnit().toMillis(config.getLimitRefreshPeriod())))
            .limitForPeriod(config.getLimitForPeriod())
            .timeoutDuration(Duration.ofMillis(config.getTimeout()))
            .build();
    return rateLimiterRegistry.rateLimiter(key, rateLimiterConfig);
  }

  private boolean shouldSendWarning(final String identifier, final RateLimitConfig config) {
    final Instant now = Instant.now();
    final Instant lastWarning = this.lastWarningTimestamps.get(identifier);

    if (lastWarning == null) {
      return true;
    }

    final long refreshPeriodMillis = config.getTimeUnit().toMillis(config.getLimitRefreshPeriod());
    final Duration timeSinceLastWarning = Duration.between(lastWarning, now);

    return timeSinceLastWarning.toMillis() >= refreshPeriodMillis;
  }
}
