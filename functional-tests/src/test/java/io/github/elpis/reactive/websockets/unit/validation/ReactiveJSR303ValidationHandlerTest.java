package io.github.elpis.reactive.websockets.unit.validation;

import io.github.elpis.reactive.websockets.config.context.ReactiveWebSocketValidationConfiguration;
import io.github.elpis.reactive.websockets.handler.validation.ReactiveJSR303ValidationHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ReactiveWebSocketValidationConfiguration.class)
class ReactiveJSR303ValidationHandlerTest {
  private static final String MODEL = "Tesla Model S";
  private static final String BLANK = "";

  @Autowired private ReactiveJSR303ValidationHandler validationHandler;

  @Test
  void testValidationWithMonoPasses() {
    // given
    final Car car = new Car(MODEL);

    // test
    final Mono<Car> validated = validationHandler.validate(car);

    // then
    StepVerifier.create(validated)
        .expectNext(car)
        .expectComplete()
        .verify(StepVerifier.DEFAULT_VERIFY_TIMEOUT);
  }

  @Test
  void testValidationWithMonoFails() {
    // given
    final Car car = new Car(BLANK);

    // test
    final Mono<Car> validated = validationHandler.validate(car);

    // then
    StepVerifier.create(validated)
        .expectError(ConstraintViolationException.class)
        .verify(StepVerifier.DEFAULT_VERIFY_TIMEOUT);
  }

  @Test
  void testValidationWithFluxPasses() {
    // given
    final Car car = new Car(MODEL);

    // test
    final Flux<Car> validated = validationHandler.validateFlux(car);

    // then
    StepVerifier.create(validated)
        .expectNext(car)
        .expectComplete()
        .verify(StepVerifier.DEFAULT_VERIFY_TIMEOUT);
  }

  @Test
  void testValidationWithFluxFails() {
    // given
    final Car car = new Car(BLANK);

    // test
    final Flux<Car> validated = validationHandler.validateFlux(car);

    // then
    StepVerifier.create(validated)
        .expectError(ConstraintViolationException.class)
        .verify(StepVerifier.DEFAULT_VERIFY_TIMEOUT);
  }

  record Car(@NotBlank String model) {}
}
