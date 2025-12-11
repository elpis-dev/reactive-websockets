package io.github.elpis.reactive.websockets.exception;

/**
 * Exception class used generally for pre-launch Spring configuration processes failures.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class WebSocketProcessingException extends RuntimeException {

  public WebSocketProcessingException(final String message) {
    super(message);
  }

  public WebSocketProcessingException(final String message, Object... args) {
    super(String.format(message, args));
  }
}
