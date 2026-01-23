package io.github.elpis.reactive.websockets.context.advice;

import io.github.elpis.reactive.websockets.web.annotation.WebSocketAdvice;
import java.io.IOException;
import java.util.Map;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Flux;

/**
 * Global WebSocket error handler. Handles exceptions thrown by any @MessageEndpoint that don't have
 * local handlers.
 */
@WebSocketAdvice
public class GlobalWebSocketErrorHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public Map<String, Object> handleIllegalArgument(final IllegalArgumentException ex) {
    return Map.of("error", "INVALID_ARGUMENT", "message", ex.getMessage());
  }

  @ExceptionHandler({RuntimeException.class, Exception.class})
  public Flux<Map<String, Object>> handleGenericError(final Exception __) {
    return Flux.just(
        Map.of(
            "error", "INTERNAL_ERROR",
            "message", "An unexpected error occurred"));
  }

  @ExceptionHandler
  public void handleIOException(final IOException __) {
    // Void handler - just logs, doesn't send response to client
  }
}
