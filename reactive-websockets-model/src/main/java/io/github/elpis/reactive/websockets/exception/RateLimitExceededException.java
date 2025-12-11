package io.github.elpis.reactive.websockets.exception;

/**
 * Exception thrown when a WebSocket message rate limit is exceeded.
 *
 * <p>This exception is thrown when a client sends messages too quickly and exceeds the configured
 * rate limit for the endpoint.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class RateLimitExceededException extends RuntimeException {

  public RateLimitExceededException(final String message) {
    super(message);
  }

  public RateLimitExceededException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
