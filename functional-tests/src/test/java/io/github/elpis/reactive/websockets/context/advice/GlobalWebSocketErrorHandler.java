package io.github.elpis.reactive.websockets.context.advice;

import io.github.elpis.reactive.websockets.web.annotation.WebSocketAdvice;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * Global WebSocket error handler. Handles exceptions thrown by any @MessageEndpoint that don't have
 * local handlers.
 */
@WebSocketAdvice
public class GlobalWebSocketErrorHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalWebSocketErrorHandler.class);

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Handling IllegalArgumentException globally: {}", ex.getMessage());
    return Mono.just(Map.of("error", "INVALID_ARGUMENT", "message", ex.getMessage()));
  }

  @ExceptionHandler({RuntimeException.class, Exception.class})
  public Mono<Map<String, Object>> handleGenericError(Exception ex) {
    log.error("Handling generic exception globally: {}", ex.getMessage(), ex);
    return Mono.just(
        Map.of(
            "error", "INTERNAL_ERROR",
            "message", "An unexpected error occurred"));
  }

  @ExceptionHandler
  public void handleIOException(java.io.IOException ex) {
    // Void handler - just logs, doesn't send response to client
    log.error("IO Exception occurred: {}", ex.getMessage(), ex);
  }
}
