package io.github.elpis.reactive.websockets.exception;

/**
 * Exception class used generally for validation failures.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class WebSocketValidationException extends RuntimeException {

  public WebSocketValidationException(final String message) {
    super(message);
  }
}
