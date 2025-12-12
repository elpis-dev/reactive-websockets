package io.github.elpis.reactive.websockets.exception;

/**
 * Exception thrown when backpressure handling is configured with ERROR strategy and the client
 * cannot keep up with the message rate.
 *
 * <p>This exception is triggered by the {@code onBackpressureError()} operator when the downstream
 * consumer is not requesting fast enough and messages cannot be delivered.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class BackpressureException extends RuntimeException {

  /**
   * Constructs a new BackpressureException with a default message.
   *
   * @param sessionId the WebSocket session ID experiencing backpressure
   */
  @SuppressWarnings("unused")
  public BackpressureException(final String sessionId) {
    super(
        String.format(
            "Backpressure detected on WebSocket session '%s'. "
                + "Client is not consuming messages fast enough.",
            sessionId));
  }

  /**
   * Constructs a new BackpressureException with a custom message.
   *
   * @param message the detail message
   */
  @SuppressWarnings("unused")
  public BackpressureException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
