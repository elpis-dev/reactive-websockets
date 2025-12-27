package io.github.elpis.reactive.websockets.session;

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
 * @since 1.1.0
 */
public final class WebSocketSessionRegistry {

  private static final Logger log = LoggerFactory.getLogger(WebSocketSessionRegistry.class);

  private final ConcurrentHashMap<String, PathSessions> pathRegistry = new ConcurrentHashMap<>();
  private final AtomicLong totalSessions = new AtomicLong(0);

  /**
   * Registers a new WebSocket session with its reactive streams.
   *
   * @param path the WebSocket path (e.g., "/chat/123")
   * @param sessionId the unique session identifier
   * @param streams the session streams containing Sinks and Flux views
   * @return the registered SessionStreams
   */
  public SessionStreams registerSession(String path, String sessionId, SessionStreams streams) {
    log.debug("Registering session {} for path {}", sessionId, path);

    PathSessions pathSessions = pathRegistry.computeIfAbsent(path, PathSessions::new);
    pathSessions.addSession(sessionId, streams);
    totalSessions.incrementAndGet();

    return streams;
  }

  /**
   * Unregisters a WebSocket session and cleans up its resources.
   *
   * @param path the WebSocket path
   * @param sessionId the session identifier to remove
   * @return Optional containing the removed SessionStreams, or empty if not found
   */
  public Optional<SessionStreams> unregisterSession(String path, String sessionId) {
    log.debug("Unregistering session {} from path {}", sessionId, path);

    return Optional.ofNullable(pathRegistry.get(path))
        .flatMap(
            pathSessions -> {
              Optional<SessionStreams> removed = pathSessions.removeSession(sessionId);

              if (pathSessions.isEmpty()) {
                pathRegistry.remove(path);
                log.debug("Removed empty path: {}", path);
              }

              removed.ifPresent(s -> totalSessions.decrementAndGet());
              return removed;
            });
  }

  /**
   * Gets a specific session's streams.
   *
   * @param path the WebSocket path
   * @param sessionId the session identifier
   * @return Optional containing the SessionStreams, or empty if not found
   */
  public Optional<SessionStreams> getSession(String path, String sessionId) {
    return Optional.ofNullable(pathRegistry.get(path)).flatMap(ps -> ps.getSession(sessionId));
  }

  /**
   * Gets all sessions for a specific path.
   *
   * @param path the WebSocket path
   * @return collection of all SessionStreams for the path (empty if path not found)
   */
  public Collection<SessionStreams> getAllSessions(String path) {
    return Optional.ofNullable(pathRegistry.get(path))
        .map(PathSessions::getAllSessions)
        .orElse(java.util.Collections.emptyList());
  }

  /** Gets the total number of active sessions across all paths. */
  public long getTotalSessionCount() {
    return totalSessions.get();
  }

  /** Gets the number of sessions for a specific path. */
  public long getSessionCount(String path) {
    return Optional.ofNullable(pathRegistry.get(path))
        .map(PathSessions::getSessionCount)
        .orElse(0L);
  }

  /** Gets all active paths. */
  public Collection<String> getAllPaths() {
    return pathRegistry.keySet();
  }

  /**
   * Scheduled task to cleanup orphaned sessions (failsafe).
   *
   * <p>This runs periodically to detect sessions that are closed but weren't properly unregistered.
   * This is a safety net - proper cleanup should happen in {@code doFinally()} handlers.
   *
   * <p>Schedule this with Spring's {@code @Scheduled(fixedDelay = 60000)}
   */
  public void cleanupOrphanedSessions() {
    log.debug("Running orphaned session cleanup check");
    int cleaned = 0;

    for (String path : getAllPaths()) {
      Collection<SessionStreams> sessions = getAllSessions(path);

      for (SessionStreams streams : sessions) {
        if (!streams.metadata().isOpen()) {
          log.warn(
              "Found orphaned session (isOpen=false), cleaning up: {} on path {}",
              streams.metadata().getSessionId(),
              path);

          unregisterSession(path, streams.metadata().getSessionId());
          cleaned++;
        }
      }
    }

    if (cleaned > 0) {
      log.info("Cleaned up {} orphaned sessions", cleaned);
    }
  }

  /** Cleanup all sessions (for graceful shutdown). */
  public void shutdown() {
    log.info("Shutting down WebSocketSessionRegistry with {} sessions", totalSessions.get());

    pathRegistry.values().forEach(PathSessions::shutdown);
    pathRegistry.clear();
    totalSessions.set(0);
  }
}
