package io.github.elpis.reactive.websockets.config.mapper;

import io.github.elpis.reactive.websockets.flowcontrol.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.flowcontrol.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapperFactory;
import io.github.elpis.reactive.websockets.mapper.impl.BackpressureAnnotationMapper;
import io.github.elpis.reactive.websockets.mapper.impl.HeartbeatAnnotationMapper;
import io.github.elpis.reactive.websockets.mapper.impl.RateLimitAnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import java.lang.annotation.Annotation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveWebSocketMappingConfiguration {
  @Bean
  public AnnotationMapper<Backpressure, BackpressureConfig> backpressureAnnotationMapper() {
    return new BackpressureAnnotationMapper();
  }

  @Bean
  public AnnotationMapper<Heartbeat, HeartbeatConfig> heartbeatAnnotationMapper() {
    return new HeartbeatAnnotationMapper();
  }

  @Bean
  public AnnotationMapper<RateLimit, RateLimitConfig> rateLimitAnnotationMapper() {
    return new RateLimitAnnotationMapper();
  }

  @Bean
  public AnnotationMapperFactory annotationMapperFactory(
      final ObjectProvider<AnnotationMapper<? extends Annotation, ?>> mappers) {
    return new AnnotationMapperFactory(mappers.stream().toList());
  }
}
