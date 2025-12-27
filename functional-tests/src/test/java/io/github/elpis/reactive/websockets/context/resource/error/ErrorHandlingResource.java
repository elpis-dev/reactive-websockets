package io.github.elpis.reactive.websockets.context.resource.error;

import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Test resource for error handling functionality. Tests both local and global exception handlers.
 */
@MessageEndpoint("/error-test")
public class ErrorHandlingResource {

  private static final Logger log = LoggerFactory.getLogger(ErrorHandlingResource.class);

  /** Endpoint that throws IllegalArgumentException to test local handler. */
  @OnMessage(value = "/local-error")
  public Flux<String> throwLocalError(@RequestBody Flux<String> messages) {
    return messages
        .doOnNext(msg -> log.info("Processing message for local error: {}", msg))
        .flatMap(
            msg -> {
              if (msg.contains("trigger-local")) {
                return Flux.error(new IllegalStateException("Local error triggered: " + msg));
              }
              return Mono.just("Processed: " + msg);
            });
  }

  /** Endpoint that throws IllegalArgumentException to test global handler. */
  @OnMessage(value = "/global-error")
  public Flux<String> throwGlobalError(@RequestBody Flux<String> messages) {
    return messages
        .doOnNext(msg -> log.info("Processing message for global error: {}", msg))
        .flatMap(
            msg -> {
              if (msg.contains("trigger-global")) {
                return Flux.error(new IllegalArgumentException("Global error triggered: " + msg));
              }
              return Mono.just("Processed: " + msg);
            });
  }

  /** Endpoint that throws RuntimeException to test global fallback handler. */
  @OnMessage(value = "/runtime-error")
  public Flux<String> throwRuntimeError(@RequestBody Flux<String> messages) {
    return messages
        .doOnNext(msg -> log.info("Processing message for runtime error: {}", msg))
        .flatMap(
            msg -> {
              if (msg.contains("trigger-runtime")) {
                return Flux.error(new RuntimeException("Runtime error triggered: " + msg));
              }
              return Mono.just("Processed: " + msg);
            });
  }

  /** Endpoint that throws IOException to test void global handler. */
  @OnMessage(value = "/io-error")
  public Flux<String> throwIOError(@RequestBody Flux<String> messages) {
    return messages
        .doOnNext(msg -> log.info("Processing message for IO error: {}", msg))
        .flatMap(
            msg -> {
              if (msg.contains("trigger-io")) {
                return Flux.error(
                    new RuntimeException(new java.io.IOException("IO error triggered: " + msg)));
              }
              return Mono.just("Processed: " + msg);
            });
  }

  /** Endpoint that works normally without errors. */
  @OnMessage(value = "/no-error")
  public Flux<String> noError(@RequestBody Flux<String> messages) {
    return messages
        .doOnNext(msg -> log.info("Processing message (no error): {}", msg))
        .map(msg -> "Echo: " + msg);
  }

  /**
   * Local exception handler for IllegalStateException. This should be called instead of the global
   * handler.
   */
  @ExceptionHandler
  public Mono<Map<String, Object>> handleLocalIllegalState(IllegalStateException ex) {
    log.warn("Handling IllegalStateException locally: {}", ex.getMessage());
    return Mono.just(
        Map.of(
            "error", "LOCAL_STATE_ERROR",
            "message", ex.getMessage(),
            "handled_by", "local"));
  }
}
