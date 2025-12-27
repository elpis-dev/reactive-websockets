package io.github.elpis.reactive.websockets.session;

import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Immutable record containing reactive streams for a WebSocket session.
 *
 * <p>Single Responsibility: Hold inbound/outbound sinks and metadata.
 *
 * <p>Contains:
 *
 * <ul>
 *   <li>Inbound Sink: For publishing incoming WebSocketMessages from client
 *   <li>Outbound Sink: For publishing outgoing Objects to client (converted by handler)
 *   <li>Metadata: Session information (ID, timestamp, etc.)
 * </ul>
 *
 * <p>Flux views are created on-demand via {@link #inboundFlux()} and {@link #outboundFlux()}
 * methods rather than stored, reducing memory overhead and simplifying lifecycle management.
 *
 * @since 1.1.0
 */
public record SessionStreams(
    Sinks.Many<WebSocketMessage> inboundSink,
    Sinks.Many<Object> outboundSink,
    ReactiveWebSocketSession metadata) {

  /**
   * Factory method to create SessionStreams with proper Sink configuration.
   *
   * @param session the reactive WebSocket session metadata
   * @return new SessionStreams instance
   */
  public static SessionStreams create(ReactiveWebSocketSession session) {
    Sinks.Many<WebSocketMessage> inbound =
        Sinks.many().multicast().onBackpressureBuffer(256, false);

    Sinks.Many<Object> outbound = Sinks.many().multicast().onBackpressureBuffer(256, false);

    return new SessionStreams(inbound, outbound, session);
  }

  /**
   * Gets the inbound Flux view.
   *
   * <p>This method can be called multiple times - all calls return views of the same underlying
   * Sink and will receive the same events.
   *
   * @return Flux view of incoming WebSocketMessages from the client
   */
  public Flux<WebSocketMessage> inboundFlux() {
    return inboundSink.asFlux();
  }

  /**
   * Gets the outbound Flux view.
   *
   * <p>This method can be called multiple times - all calls return views of the same underlying
   * Sink and will receive the same events.
   *
   * <p>Note: The Flux contains Object types. Type conversion to WebSocketMessage is handled by
   * BaseWebSocketHandler.mapOutput() when wiring to WebSocket.send().
   *
   * @return Flux view of outgoing Objects to the client
   */
  public Flux<Object> outboundFlux() {
    return outboundSink.asFlux();
  }

  /** Closes the session streams gracefully. */
  public void close() {
    inboundSink.tryEmitComplete();
    outboundSink.tryEmitComplete();
  }
}
