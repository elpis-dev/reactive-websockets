package io.github.elpis.reactive.websockets.session;

import static io.github.elpis.reactive.websockets.Constants.NO_SESSIONS;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central registry for WebSocket session streams.
 *
 * <p>Maintains a two-level map structure:
 *
 * <ul>
 *   <li>Level 1: WebSocket path -> PathSessions
 *   <li>Level 2: Session ID -> SessionStreams (Sinks + Flux views)
 * </ul>
 *
 * <p>Thread-safe and designed for high concurrency.
 *
 * @since 1.0.0
 */
public final class ReactiveWebSocketSessionRegistry {
  private static final Logger log = LoggerFactory.getLogger(ReactiveWebSocketSessionRegistry.class);

  private final ConcurrentHashMap<String, ReactiveWebSocketPathSessions> pathRegistry =
      new ConcurrentHashMap<>();
  private final AtomicLong totalSessions = new AtomicLong(NO_SESSIONS);

  /**
   * Registers a new WebSocket session with its reactive streams.
   *
   * @param path the WebSocket path (e.g., "/chat/123")
   * @param sessionId the unique session identifier
   * @param streams the session streams containing Sinks and Flux views
   * @since 1.0.0
   */
  public void register(final String path, final String sessionId, final SessionStreams streams) {
    if (log.isDebugEnabled()) {
      log.debug("Registering session {} for path {}", sessionId, path);
    }

    ReactiveWebSocketPathSessions reactiveWebSocketPathSessions =
        pathRegistry.computeIfAbsent(path, ReactiveWebSocketPathSessions::new);
    reactiveWebSocketPathSessions.add(sessionId, streams);
    totalSessions.incrementAndGet();
  }

  /**
   * Unregisters a WebSocket session and cleans up its resources.
   *
   * @param path the WebSocket path
   * @param sessionId the session identifier to remove
   * @since 1.0.0
   */
  public void unregister(final String path, final String sessionId) {
    if (log.isDebugEnabled()) {
      log.debug("Unregistering session {} from path {}", sessionId, path);
    }

    Optional.ofNullable(pathRegistry.get(path))
        .ifPresent(
            reactiveWebSocketPathSessions -> {
              Optional<SessionStreams> removed = reactiveWebSocketPathSessions.remove(sessionId);

              if (reactiveWebSocketPathSessions.isEmpty()) {
                pathRegistry.remove(path);
                if (log.isDebugEnabled()) {
                  log.debug("Removed empty path: {}", path);
                }
              }

              removed.ifPresent(s -> totalSessions.decrementAndGet());
            });
  }

  /**
   * Gets a specific session's streams.
   *
   * @param path the WebSocket path
   * @param sessionId the session identifier
   * @return Optional containing the SessionStreams, or empty if not found
   * @since 1.0.0
   */
  public Optional<SessionStreams> getSession(final String path, final String sessionId) {
    return Optional.ofNullable(pathRegistry.get(path)).flatMap(ps -> ps.getSession(sessionId));
  }

  /**
   * Gets all sessions for a specific path.
   *
   * @param path the WebSocket path
   * @return collection of all SessionStreams for the path (empty if path not found)
   * @since 1.0.0
   */
  public Collection<SessionStreams> getAllSessions(String path) {
    return Optional.ofNullable(pathRegistry.get(path))
        .map(ReactiveWebSocketPathSessions::getAllSessions)
        .orElse(java.util.Collections.emptyList());
  }

  /**
   * Gets the total number of active sessions across all paths.
   *
   * @return total session count
   * @since 1.0.0
   */
  public long getTotalSessionCount() {
    return totalSessions.get();
  }

  /**
   * Gets the number of sessions for a specific path.
   *
   * @param path the WebSocket path
   * @return session count for the path
   * @since 1.0.0
   */
  public long getSessionCount(String path) {
    return Optional.ofNullable(pathRegistry.get(path))
        .map(ReactiveWebSocketPathSessions::getSessionCount)
        .orElse((long) NO_SESSIONS);
  }

  /**
   * Gets all active paths.
   *
   * @return collection of active WebSocket paths
   * @since 1.0.0
   */
  public Collection<String> getAllPaths() {
    return pathRegistry.keySet();
  }

  ConcurrentHashMap<String, ReactiveWebSocketPathSessions> getPathRegistry() {
    return pathRegistry;
  }

  void resetSessionCount() {
    totalSessions.set(NO_SESSIONS);
  }
}
