package io.github.elpis.reactive.websockets.config.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.elpis.reactive.websockets.flowcontrol.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.flowcontrol.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapperFactory;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.mapper.impl.BackpressureAnnotationMapper;
import io.github.elpis.reactive.websockets.mapper.impl.HeartbeatAnnotationMapper;
import io.github.elpis.reactive.websockets.mapper.impl.RateLimitAnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import java.lang.annotation.Annotation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for mapping reactive WebSocket annotations to their respective flow control
 * configurations.
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Configuration
public class ReactiveWebSocketMappingConfiguration {

  /**
   * Creates a bean for mapping the {@link Backpressure} annotation to its corresponding {@link
   * BackpressureConfig}.
   *
   * @return the BackpressureAnnotationMapper bean
   */
  @Bean
  public AnnotationMapper<Backpressure, BackpressureConfig> backpressureAnnotationMapper() {
    return new BackpressureAnnotationMapper();
  }

  /**
   * Creates a bean for mapping the {@link Heartbeat} annotation to its corresponding {@link
   * HeartbeatConfig}.
   *
   * @return the HeartbeatAnnotationMapper bean
   */
  @Bean
  public AnnotationMapper<Heartbeat, HeartbeatConfig> heartbeatAnnotationMapper() {
    return new HeartbeatAnnotationMapper();
  }

  /**
   * Creates a bean for mapping the {@link RateLimit} annotation to its corresponding {@link
   * RateLimitConfig}.
   *
   * @return the RateLimitAnnotationMapper bean
   */
  @Bean
  public AnnotationMapper<RateLimit, RateLimitConfig> rateLimitAnnotationMapper() {
    return new RateLimitAnnotationMapper();
  }

  /**
   * Creates an AnnotationMapperFactory that aggregates all available AnnotationMappers.
   *
   * @param mappers the ObjectProvider of AnnotationMappers
   * @return the AnnotationMapperFactory bean
   */
  @Bean
  public AnnotationMapperFactory annotationMapperFactory(
      final ObjectProvider<AnnotationMapper<? extends Annotation, ?>> mappers) {
    return new AnnotationMapperFactory(mappers.stream().toList());
  }

  /**
   * Creates a default ObjectMapper bean if none is already defined in the context.
   *
   * @return the ObjectMapper bean
   */
  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  /**
   * Creates a JsonMapper bean using the provided ObjectMapper.
   *
   * @param objectMapper the ObjectMapper to be used by JsonMapper
   * @return the JsonMapper bean
   */
  @Bean
  public JsonMapper jsonMapper(final ObjectMapper objectMapper) {
    return new JsonMapper(objectMapper);
  }
}
