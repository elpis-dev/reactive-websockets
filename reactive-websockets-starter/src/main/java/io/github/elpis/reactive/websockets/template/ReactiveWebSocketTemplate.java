package io.github.elpis.reactive.websockets.template;

import io.github.elpis.reactive.websockets.handler.exception.ErrorResponseException;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import java.util.Collection;
import java.util.Set;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Template for sending messages to WebSocket sessions.
 *
 * <p>Provides convenient methods for broadcasting and sending to specific sessions. Inject this
 * bean into your services to push messages to connected clients.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * @Service
 * public class NotificationService {
 *     private final ReactiveWebSocketTemplate template;
 *
 *     public NotificationService(ReactiveWebSocketTemplate template) {
 *         this.template = template;
 *     }
 *
 *     // Broadcast to all connected clients on a path
 *     public Mono<Void> notifyAll(String message) {
 *         return template.sendBroadcast("/ws/notifications", message);
 *     }
 *
 *     // Send to a specific session
 *     public Mono<Void> notifyUser(String sessionId, Notification notification) {
 *         return template.sendToSession("/ws/notifications", sessionId, notification);
 *     }
 *
 *     // Send error response
 *     public Mono<Void> sendError(String sessionId, ErrorResponse error) {
 *         return template.sendError("/ws/notifications", sessionId, error);
 *     }
 * }
 * }</pre>
 *
 * <p>The template accepts any Object type. Type conversion to WebSocketMessage is handled
 * automatically by the handler infrastructure.
 *
 * @since 1.1.0
 */
public class ReactiveWebSocketTemplate {

  private static final Logger log = LoggerFactory.getLogger(ReactiveWebSocketTemplate.class);

  private final WebSocketSessionRegistry registry;

  public ReactiveWebSocketTemplate(WebSocketSessionRegistry registry) {
    this.registry = registry;
  }

  /**
   * Broadcasts a message to all sessions on a path.
   *
   * @param path the WebSocket path
   * @param payload the message payload (any Object - converted by handler)
   * @return Mono that completes when message is broadcast
   */
  public Mono<Void> sendBroadcast(String path, Object payload) {
    return Mono.fromRunnable(
        () -> {
          Collection<SessionStreams> sessions = registry.getAllSessions(path);

          for (SessionStreams streams : sessions) {
            Sinks.EmitResult result = streams.outboundSink().tryEmitNext(payload);
            if (result.isFailure()) {
              log.warn(
                  "Failed to broadcast to session {} on path {}: {}",
                  streams.metadata().getSessionId(),
                  path,
                  result);
            }
          }

          log.debug("Broadcast completed to {} sessions on path {}", sessions.size(), path);
        });
  }

  /**
   * Broadcasts a stream of messages to all sessions on a path.
   *
   * @param path the WebSocket path
   * @param messages Publisher of message payloads
   * @return Mono that completes when all messages are broadcast
   */
  public Mono<Void> sendBroadcast(String path, Publisher<?> messages) {
    return Flux.from(messages).flatMap(msg -> sendBroadcast(path, msg)).then();
  }

  /**
   * Sends a message to a specific session.
   *
   * @param path the WebSocket path
   * @param sessionId the target session ID
   * @param payload the message payload (any Object - converted by handler)
   * @return Mono that completes when message is sent, or error if session not found
   */
  public Mono<Void> sendToSession(String path, String sessionId, Object payload) {
    return Mono.fromRunnable(
        () -> {
          SessionStreams streams =
              registry
                  .getSession(path, sessionId)
                  .orElseThrow(
                      () ->
                          new SessionNotFoundException(
                              String.format("Session %s not found on path %s", sessionId, path)));

          Sinks.EmitResult result = streams.outboundSink().tryEmitNext(payload);
          if (result.isFailure()) {
            log.warn("Failed to send message to session {}: {}", sessionId, result);
          }
        });
  }

  /**
   * Sends a stream of messages to a specific session.
   *
   * @param path the WebSocket path
   * @param sessionId the target session ID
   * @param messages Publisher of message payloads
   * @return Mono that completes when all messages are sent
   */
  public Mono<Void> sendToSession(String path, String sessionId, Publisher<?> messages) {
    return Flux.from(messages).flatMap(msg -> sendToSession(path, sessionId, msg)).then();
  }

  /**
   * Sends a message to multiple specific sessions.
   *
   * @param path the WebSocket path
   * @param sessionIds set of target session IDs
   * @param payload the message payload (any Object - converted by handler)
   * @return Mono that completes when all messages are sent
   */
  public Mono<Void> sendToSessions(String path, Set<String> sessionIds, Object payload) {
    return Flux.fromIterable(sessionIds)
        .flatMap(
            sessionId ->
                sendToSession(path, sessionId, payload)
                    .onErrorResume(
                        SessionNotFoundException.class,
                        e -> {
                          log.debug("Skipping non-existent session: {}", sessionId);
                          return Mono.empty();
                        }))
        .then();
  }

  /**
   * Sends an error response to a specific session.
   *
   * <p>The error payload is wrapped in ErrorResponseException and emitted via tryEmitError().
   * BaseWebSocketHandler catches this, extracts the payload, converts and sends to client.
   *
   * @param path the WebSocket path
   * @param sessionId the target session ID
   * @param errorPayload the error response object to send
   * @return Mono that completes when error is emitted
   */
  public Mono<Void> sendError(String path, String sessionId, Object errorPayload) {
    return Mono.fromRunnable(
        () -> {
          SessionStreams streams =
              registry
                  .getSession(path, sessionId)
                  .orElseThrow(
                      () ->
                          new SessionNotFoundException(
                              String.format("Session %s not found on path %s", sessionId, path)));

          Sinks.EmitResult result =
              streams.outboundSink().tryEmitError(new ErrorResponseException(errorPayload));
          if (result.isFailure()) {
            log.warn("Failed to send error to session {}: {}", sessionId, result);
          }
        });
  }

  /**
   * Broadcasts an error response to all sessions on a path.
   *
   * @param path the WebSocket path
   * @param errorPayload the error response object to send
   * @return Mono that completes when error is broadcast
   */
  public Mono<Void> broadcastError(String path, Object errorPayload) {
    return Mono.fromRunnable(
        () -> {
          Collection<SessionStreams> sessions = registry.getAllSessions(path);
          for (SessionStreams streams : sessions) {
            Sinks.EmitResult result =
                streams.outboundSink().tryEmitError(new ErrorResponseException(errorPayload));
            if (result.isFailure()) {
              log.warn(
                  "Failed to broadcast error to session {} on path {}: {}",
                  streams.metadata().getSessionId(),
                  path,
                  result);
            }
          }
        });
  }
}

/** Exception thrown when attempting to send to a non-existent session. */
class SessionNotFoundException extends RuntimeException {
  SessionNotFoundException(String message) {
    super(message);
  }
}
