package io.github.elpis.reactive.websockets.config;

import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.template.ReactiveWebSocketTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for ReactiveWebSocketTemplate.
 *
 * <p>Automatically registers a ReactiveWebSocketTemplate bean in the Spring context if one is not
 * already defined.
 *
 * @since 1.1.0
 */
@Configuration
public class ReactiveWebSocketTemplateAutoConfiguration {

  /**
   * Creates a ReactiveWebSocketTemplate bean if not already present.
   *
   * @param registry the WebSocketSessionRegistry
   * @return configured ReactiveWebSocketTemplate
   */
  @Bean
  @ConditionalOnMissingBean
  public ReactiveWebSocketTemplate reactiveWebSocketTemplate(WebSocketSessionRegistry registry) {
    return new ReactiveWebSocketTemplate(registry);
  }
}
