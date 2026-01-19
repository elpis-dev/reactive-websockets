package io.github.elpis.reactive.websockets.handler.flowcontrol.impl;

import static io.github.elpis.reactive.websockets.Constants.DEFAULT_KEY;

import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPlacement;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPolicy;
import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveRateLimitFlowControlRegistry;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveRateLimitFlowControlPolicy implements FlowControlPolicy {
  private static final Logger log =
      LoggerFactory.getLogger(ReactiveRateLimitFlowControlPolicy.class);

  private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

  private final ReactiveRateLimitFlowControlRegistry reactiveRateLimitFlowControlRegistry;

  public ReactiveRateLimitFlowControlPolicy(
      final ReactiveRateLimitFlowControlRegistry reactiveRateLimitFlowControlRegistry) {
    this.reactiveRateLimitFlowControlRegistry = reactiveRateLimitFlowControlRegistry;
  }

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
              return this.applyRateLimit(rateLimiter, webSocketMessageFlux, identifier);
            })
        .orElse(webSocketMessageFlux);
  }

  @Override
  public FlowControlPlacement placement() {
    return FlowControlPlacement.INPUT;
  }

  private Flux<WebSocketMessage> applyRateLimit(
      final RateLimiter rateLimiter,
      final Flux<WebSocketMessage> webSocketMessageFlux,
      final String identifier) {
    return webSocketMessageFlux.flatMap(
        webSocketMessage -> {
          final boolean permitted = rateLimiter.acquirePermission();
          if (permitted) {
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
        yield principal != null ? principal.toString() : context.getSessionId();
      }
      case IP -> {
        final String remoteAddress = context.getRemoteAddress();
        yield remoteAddress != null ? remoteAddress : context.getSessionId();
      }
      default -> context.getSessionId();
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
}
