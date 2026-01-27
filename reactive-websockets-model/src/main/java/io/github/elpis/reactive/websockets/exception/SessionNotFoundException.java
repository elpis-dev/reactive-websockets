package io.github.elpis.reactive.websockets.exception;

/**
 * Exception thrown when a WebSocket session is not found.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class SessionNotFoundException extends RuntimeException {
  public SessionNotFoundException(String message) {
    super(message);
  }
}
