package io.github.elpis.reactive.websockets.handler.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handler for performing JSR-303 bean validation in a reactive context.
 *
 * <p>This class uses a Validator to validate objects and returns the results as reactive Mono or
 * Flux streams. If validation fails, a ConstraintViolationException is emitted.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class ReactiveJSR303ValidationHandler {
  private final Validator validator;

  public ReactiveJSR303ValidationHandler(final Validator validator) {
    this.validator = validator;
  }

  /**
   * Validates the given object and returns a Mono that emits the object if valid, or an error if
   * validation fails.
   *
   * @param <T> the type of the object to validate
   * @param object the object to validate
   * @return a Mono emitting the validated object or an error
   * @throws ConstraintViolationException if validation fails
   */
  public <T> Mono<T> validate(final T object) {
    return Mono.defer(
        () -> {
          Set<ConstraintViolation<T>> violations = validator.validate(object);
          if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
          }
          return Mono.just(object);
        });
  }

  /**
   * Validates the given object and returns a Flux that emits the object if valid, or an error if
   * validation fails.
   *
   * @param <T> the type of the object to validate
   * @param object the object to validate
   * @return a Flux emitting the validated object or an error
   * @throws ConstraintViolationException if validation fails
   */
  public <T> Flux<T> validateFlux(final T object) {
    return this.validate(object).flux();
  }
}
