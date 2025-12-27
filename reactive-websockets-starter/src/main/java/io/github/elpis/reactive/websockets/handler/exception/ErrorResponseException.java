package io.github.elpis.reactive.websockets.handler.exception;

/**
 * Exception used to signal an error response that should be sent to the client.
 *
 * <p>This exception is used as a mechanism to send error responses through the outbound sink. When
 * emitted via {@code outboundSink.tryEmitError()}, the BaseWebSocketHandler catches it, extracts
 * the payload, converts it via mapOutput(), and sends to the WebSocket.
 *
 * <p>Usage flow:
 *
 * <ol>
 *   <li>Exception handler (local or global) creates error response object
 *   <li>ReactiveWebSocketTemplate wraps it in ErrorResponseException
 *   <li>Template emits via outboundSink.tryEmitError()
 *   <li>BaseWebSocketHandler catches, extracts payload, converts and sends
 * </ol>
 *
 * @since 1.1.0
 */
public class ErrorResponseException extends RuntimeException {

  private final Object payload;

  /**
   * Creates an ErrorResponseException with the given payload.
   *
   * @param payload the error response object to send to the client
   */
  public ErrorResponseException(Object payload) {
    super("Error response: " + (payload != null ? payload.getClass().getSimpleName() : "null"));
    this.payload = payload;
  }

  /**
   * Creates an ErrorResponseException with the given payload and cause.
   *
   * @param payload the error response object to send to the client
   * @param cause the original exception that caused this error
   */
  public ErrorResponseException(Object payload, Throwable cause) {
    super(
        "Error response: " + (payload != null ? payload.getClass().getSimpleName() : "null"),
        cause);
    this.payload = payload;
  }

  /**
   * Gets the error response payload to send to the client.
   *
   * @return the payload object
   */
  public Object getPayload() {
    return payload;
  }
}
