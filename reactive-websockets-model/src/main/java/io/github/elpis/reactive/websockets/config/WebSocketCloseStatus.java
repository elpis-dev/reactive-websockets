package io.github.elpis.reactive.websockets.config;

/**
 * Static definition of common websocket close status codes. Synonym of {@link
 * org.springframework.web.reactive.socket.CloseStatus CloseStatus}.
 *
 * @author Phillip J. Fry
 * @see org.springframework.web.reactive.socket.CloseStatus
 * @since 1.0.0
 */
public enum WebSocketCloseStatus {

  /**
   * "1000 indicates a normal closure, meaning that the purpose for which the connection was
   * established has been fulfilled."
   */
  NORMAL(1000),

  /**
   * "1001 indicates that an endpoint is "going away", such as a server going down or a browser
   * having navigated away from a page."
   */
  GOING_AWAY(1001),

  /** "1002 indicates that an endpoint is terminating the connection due to a protocol error." */
  PROTOCOL_ERROR(1002),

  /**
   * "1003 indicates that an endpoint is terminating the connection because it has received a type
   * of data it cannot accept (e.g., an endpoint that understands only text data MAY send this if it
   * receives a binary message)."
   */
  NOT_ACCEPTABLE(1003),

  /**
   * "1005 is a reserved value and MUST NOT be set as a status code in a Close control frame by an
   * endpoint. It is designated for use in applications expecting a status code to indicate that no
   * status code was actually present."
   */
  NO_STATUS_CODE(1005),

  /**
   * "1006 is a reserved value and MUST NOT be set as a status code in a Close control frame by an
   * endpoint. It is designated for use in applications expecting a status code to indicate that the
   * connection was closed abnormally, e.g., without sending or receiving a Close control frame."
   */
  NO_CLOSE_FRAME(1006),

  /**
   * "1007 indicates that an endpoint is terminating the connection because it has received data
   * within a message that was not consistent with the type of the message (e.g., non-UTF-8
   * [RFC3629] data within a text message)."
   */
  BAD_DATA(1007),

  /**
   * "1008 indicates that an endpoint is terminating the connection because it has received a
   * message that violates its policy. This is a generic status code that can be returned when there
   * is no other more suitable status code (e.g., 1003 or 1009) or if there is a need to hide
   * specific details about the policy."
   */
  POLICY_VIOLATION(1008),

  /**
   * "1009 indicates that an endpoint is terminating the connection because it has received a
   * message that is too big for it to process."
   */
  TOO_BIG_TO_PROCESS(1009),

  /**
   * "1010 indicates that an endpoint (client) is terminating the connection because it has expected
   * the server to negotiate one or more extension, but the server didn't return them in the
   * response message of the WebSocket handshake. The list of extensions that are needed SHOULD
   * appear in the /reason/ part of the Close frame. Note that this status code is not used by the
   * server, because it can fail the WebSocket handshake instead."
   */
  REQUIRED_EXTENSION(1010),

  /**
   * "1011 indicates that a server is terminating the connection because it encountered an
   * unexpected condition that prevented it from fulfilling the request."
   */
  SERVER_ERROR(1011),

  /**
   * "1012 indicates that the service is restarted. A client may reconnect, and if it chooses to do,
   * should reconnect using a randomized delay of 5 - 30s."
   */
  SERVICE_RESTARTED(1012),

  /**
   * "1013 indicates that the service is experiencing overload. A client should only connect to a
   * different IP (when there are multiple for the target) or reconnect to the same IP upon user
   * action."
   */
  SERVICE_OVERLOAD(1013),

  /**
   * "1015 is a reserved value and MUST NOT be set as a status code in a Close control frame by an
   * endpoint. It is designated for use in applications expecting a status code to indicate that the
   * connection was closed due to a failure to perform a TLS handshake (e.g., the server certificate
   * can't be verified)."
   */
  TLS_HANDSHAKE_FAILURE(1015),

  ALL(0);

  private final int statusCode;

  WebSocketCloseStatus(final int statusCode) {
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public static boolean isValidCode(final int statusCode) {
    return statusCode >= 1000 && statusCode < 5000;
  }
}
