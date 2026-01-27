package io.github.elpis.reactive.websockets.config.flowcontrol;

import static io.github.elpis.reactive.websockets.Constants.DEFAULT_KEY;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPolicy;
import io.github.elpis.reactive.websockets.flowcontrol.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.flowcontrol.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.impl.ReactiveBackpressureFlowControlPolicy;
import io.github.elpis.reactive.websockets.handler.flowcontrol.impl.ReactiveRateLimitFlowControlPolicy;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveBackpressureFlowControlRegistry;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveRateLimitFlowControlRegistry;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapperFactory;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up reactive WebSocket flow control mechanisms such as
 * backpressure, rate limiting, and heartbeat.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(ReactiveWebSocketFlowControlProperties.class)
public class ReactiveWebSocketFlowControlConfiguration {
  private final ReactiveWebsocketMessageEndpointResolver processor;
  private final AnnotationMapperFactory annotationMapperFactory;

  public ReactiveWebSocketFlowControlConfiguration(
      final ReactiveWebsocketMessageEndpointResolver processor,
      final AnnotationMapperFactory annotationMapperFactory) {
    this.processor = processor;
    this.annotationMapperFactory = annotationMapperFactory;
  }

  /**
   * Creates a registry for backpressure flow control configurations based on application
   * properties.
   *
   * @param properties the ReactiveWebSocketFlowControlProperties
   * @return the ReactiveBackpressureFlowControlRegistry bean
   */
  @Bean
  @ConditionalOnProperty(
      name = "spring.webflux.reactive.websockets.flow-control.backpressure.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public ReactiveBackpressureFlowControlRegistry backpressureFlowControlRegistry(
      final ReactiveWebSocketFlowControlProperties properties) {

    final ReactiveBackpressureFlowControlRegistry registry =
        new ReactiveBackpressureFlowControlRegistry(
            this.annotationMapperFactory.getMapper(Backpressure.class), this.processor);

    final ReactiveWebSocketFlowControlProperties.BackpressurePathConfig defaultConfig =
        properties.getBackpressure().getDefaultConfig();
    if (defaultConfig != null) {
      registry.put(
          DEFAULT_KEY,
          BackpressureConfig.of(defaultConfig.getStrategy(), defaultConfig.getBufferSize()));
    }

    properties
        .getBackpressure()
        .getPaths()
        .forEach(
            (path, config) ->
                registry.put(
                    path, BackpressureConfig.of(config.getStrategy(), config.getBufferSize())));

    return registry;
  }

  /**
   * Creates a registry for rate limit flow control configurations based on application properties.
   *
   * @param properties the ReactiveWebSocketFlowControlProperties
   * @return the ReactiveRateLimitFlowControlRegistry bean
   */
  @Bean
  @ConditionalOnProperty(
      name = "spring.webflux.reactive.websockets.flow-control.rate-limit.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public ReactiveRateLimitFlowControlRegistry rateLimitFlowControlRegistry(
      final ReactiveWebSocketFlowControlProperties properties) {
    final ReactiveRateLimitFlowControlRegistry registry =
        new ReactiveRateLimitFlowControlRegistry(
            this.annotationMapperFactory.getMapper(RateLimit.class), this.processor);

    final ReactiveWebSocketFlowControlProperties.RateLimitPathConfig defaultConfig =
        properties.getRateLimit().getDefaultConfig();
    if (defaultConfig != null) {
      registry.put(
          DEFAULT_KEY,
          RateLimitConfig.of(
              defaultConfig.getLimitForPeriod(),
              defaultConfig.getLimitRefreshPeriod(),
              defaultConfig.getTimeUnit(),
              defaultConfig.getTimeout(),
              defaultConfig.getScope()));
    }

    properties
        .getRateLimit()
        .getPaths()
        .forEach(
            (path, config) ->
                registry.put(
                    path,
                    RateLimitConfig.of(
                        config.getLimitForPeriod(),
                        config.getLimitRefreshPeriod(),
                        config.getTimeUnit(),
                        config.getTimeout(),
                        config.getScope())));

    return registry;
  }

  /**
   * Creates a registry for heartbeat flow control configurations based on application properties.
   *
   * @param properties the ReactiveWebSocketFlowControlProperties
   * @return the ReactiveHeartbeatFlowControlRegistry bean
   */
  @Bean
  @ConditionalOnProperty(
      name = "spring.webflux.reactive.websockets.flow-control.heartbeat.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnMissingBean(ReactiveHeartbeatFlowControlRegistry.class)
  public ReactiveHeartbeatFlowControlRegistry heartbeatFlowControlRegistry(
      final ReactiveWebSocketFlowControlProperties properties) {
    final ReactiveHeartbeatFlowControlRegistry registry =
        new ReactiveHeartbeatFlowControlRegistry(
            this.annotationMapperFactory.getMapper(Heartbeat.class), this.processor);

    final ReactiveWebSocketFlowControlProperties.HeartbeatPathConfig defaultConfig =
        properties.getHeartbeat().getDefaultConfig();
    if (defaultConfig != null) {
      registry.put(
          DEFAULT_KEY, HeartbeatConfig.of(defaultConfig.getInterval(), defaultConfig.getTimeout()));
    }

    properties
        .getHeartbeat()
        .getPaths()
        .forEach(
            (path, config) ->
                registry.put(path, HeartbeatConfig.of(config.getInterval(), config.getTimeout())));

    return registry;
  }

  /**
   * Creates a rate limit flow control policy bean if enabled and not already defined.
   *
   * @param registry the ReactiveRateLimitFlowControlRegistry
   * @return the ReactiveRateLimitFlowControlPolicy bean
   */
  @Bean
  @ConditionalOnProperty(
      name = "spring.webflux.reactive.websockets.flow-control.rate-limit.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnMissingBean(ReactiveRateLimitFlowControlPolicy.class)
  public ReactiveRateLimitFlowControlPolicy rateLimitFlowControlPolicy(
      final ReactiveRateLimitFlowControlRegistry registry) {
    return new ReactiveRateLimitFlowControlPolicy(registry);
  }

  /**
   * Creates a backpressure flow control policy bean if enabled and not already defined.
   *
   * @param registry the ReactiveBackpressureFlowControlRegistry
   * @return the ReactiveBackpressureFlowControlPolicy bean
   */
  @Bean
  @ConditionalOnProperty(
      name = "spring.webflux.reactive.websockets.flow-control.backpressure.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnMissingBean(ReactiveBackpressureFlowControlPolicy.class)
  public ReactiveBackpressureFlowControlPolicy backpressureFlowControlPolicy(
      final ReactiveBackpressureFlowControlRegistry registry) {
    return new ReactiveBackpressureFlowControlPolicy(registry);
  }

  /**
   * Creates a builder for the reactive flow control chain, adding available policies.
   *
   * @param backpressurePolicy provider for ReactiveBackpressureFlowControlPolicy
   * @param rateLimitPolicy provider for ReactiveRateLimitFlowControlPolicy
   * @param allPolicies provider for all FlowControlPolicy beans
   * @return the ReactiveFlowControlChain.Builder bean
   */
  @Bean
  @ConditionalOnProperty(
      name = "spring.webflux.reactive.websockets.flow-control.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public ReactiveFlowControlChain.Builder flowControlChainBuilder(
      final ObjectProvider<ReactiveBackpressureFlowControlPolicy> backpressurePolicy,
      final ObjectProvider<ReactiveRateLimitFlowControlPolicy> rateLimitPolicy,
      final ObjectProvider<FlowControlPolicy> allPolicies) {
    final ReactiveFlowControlChain.Builder builder = new ReactiveFlowControlChain.Builder();

    backpressurePolicy.ifAvailable(builder::addPolicy);
    rateLimitPolicy.ifAvailable(builder::addPolicy);

    final List<FlowControlPolicy> customPolicies =
        allPolicies
            .orderedStream()
            .filter(
                p ->
                    !(p instanceof ReactiveBackpressureFlowControlPolicy)
                        && !(p instanceof ReactiveRateLimitFlowControlPolicy))
            .toList();

    return builder.addPolicies(customPolicies);
  }

  /**
   * Builds the reactive flow control chain if enabled and not already defined.
   *
   * @param builder the ReactiveFlowControlChain.Builder
   * @return the ReactiveFlowControlChain bean
   */
  @Bean
  @ConditionalOnProperty(
      name = "spring.webflux.reactive.websockets.flow-control.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnMissingBean(ReactiveFlowControlChain.class)
  public ReactiveFlowControlChain flowControlChain(final ReactiveFlowControlChain.Builder builder) {
    return builder.build();
  }

  /**
   * Provides an empty reactive flow control chain if none is defined.
   *
   * @return the empty ReactiveFlowControlChain bean
   */
  @Bean
  @ConditionalOnMissingBean(ReactiveFlowControlChain.class)
  public ReactiveFlowControlChain emptyFlowControlChain() {
    return ReactiveFlowControlChain.empty();
  }
}
