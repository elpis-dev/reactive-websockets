package io.github.elpis.reactive.websockets.config.context;

import io.github.elpis.reactive.websockets.handler.validation.ReactiveJSR303ValidationHandler;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that sets up JSR-303 validation for WebSocket messages if enabled.
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(
    name = "spring.webflux.reactive.websockets.validation.jsr303.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ReactiveWebSocketValidationConfiguration {

  /**
   * Creates a default ValidatorFactory bean if none is present in the application context.
   *
   * @return a ValidatorFactory instance
   * @since 1.0.0
   */
  @Bean
  @ConditionalOnMissingBean(ValidatorFactory.class)
  public ValidatorFactory validatorFactory() {
    return Validation.buildDefaultValidatorFactory();
  }

  /**
   * Creates a Validator bean using the provided ValidatorFactory if none is present.
   *
   * @param validatorFactory the ValidatorFactory to create the Validator from
   * @return a Validator instance
   * @since 1.0.0
   */
  @Bean
  @ConditionalOnMissingBean(Validator.class)
  public Validator validator(final ValidatorFactory validatorFactory) {
    return validatorFactory.getValidator();
  }

  /**
   * Creates a ReactiveJSR303ValidationHandler bean using the provided Validator.
   *
   * @param validator the Validator to be used for validation
   * @return a ReactiveJSR303ValidationHandler instance
   * @since 1.0.0
   */
  @Bean
  public ReactiveJSR303ValidationHandler jsr303ValidationHandler(final Validator validator) {
    return new ReactiveJSR303ValidationHandler(validator);
  }
}
