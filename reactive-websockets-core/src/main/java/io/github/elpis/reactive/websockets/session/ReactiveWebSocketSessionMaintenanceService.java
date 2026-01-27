package io.github.elpis.reactive.websockets.session;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for maintaining and cleaning up WebSocket sessions.
 *
 * <p>This service provides functionality to periodically clean up orphaned sessions and to
 * gracefully shut down all sessions when needed.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class ReactiveWebSocketSessionMaintenanceService {
  private static final Logger log =
      LoggerFactory.getLogger(ReactiveWebSocketSessionMaintenanceService.class);

  private final ReactiveWebSocketSessionRegistry registry;

  public ReactiveWebSocketSessionMaintenanceService(ReactiveWebSocketSessionRegistry registry) {
    this.registry = registry;
  }

  /**
   * Scheduled task to clean up orphaned sessions (failsafe).
   *
   * <p>This runs periodically to detect sessions that are closed but weren't properly unregistered.
   * This is a safety net - proper cleanup should happen in {@code doFinally()} handlers.
   *
   * <p>Schedule this with Spring's {@code @Scheduled(fixedDelay = 60000)}
   *
   * @since 1.0.0
   */
  public void cleanupOrphanedSessions() {
    if (log.isDebugEnabled()) {
      log.debug("Running orphaned session cleanup check");
    }

    int cleaned = 0;
    for (final String path : this.registry.getAllPaths()) {
      final Collection<SessionStreams> sessions = this.registry.getAllSessions(path);

      for (SessionStreams streams : sessions) {
        if (!streams.metadata().isOpen()) {
          if (log.isDebugEnabled()) {
            log.debug(
                "Found orphaned session (isOpen=false), cleaning up: {} on path {}",
                streams.metadata().getSessionId(),
                path);
          }

          this.registry.unregister(path, streams.metadata().getSessionId());
          cleaned++;
        }
      }
    }

    if (cleaned > 0) {
      if (log.isDebugEnabled()) {
        log.debug("Cleaned up {} orphaned sessions", cleaned);
      }
    }
  }

  /**
   * Cleanup all sessions (for graceful shutdown).
   *
   * @since 1.0.0
   */
  public void shutdown() {
    if (log.isDebugEnabled()) {
      log.debug(
          "Shutting down WebSocketSessionRegistry with {} sessions",
          this.registry.getTotalSessionCount());
    }

    final ConcurrentHashMap<String, ReactiveWebSocketPathSessions> pathRegistry =
        this.registry.getPathRegistry();
    pathRegistry
        .values()
        .forEach(
            reactiveWebSocketPathSessions -> {
              reactiveWebSocketPathSessions.getAllSessions().forEach(SessionStreams::close);
              reactiveWebSocketPathSessions.resetSessions();
            });

    pathRegistry.clear();

    this.registry.resetSessionCount();
  }
}
