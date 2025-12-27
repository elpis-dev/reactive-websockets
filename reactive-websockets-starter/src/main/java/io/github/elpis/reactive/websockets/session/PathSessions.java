package io.github.elpis.reactive.websockets.session;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all sessions for a specific WebSocket path.
 *
 * @since 1.1.0
 */
class PathSessions {

  private static final Logger log = LoggerFactory.getLogger(PathSessions.class);

  private final String path;
  private final ConcurrentHashMap<String, SessionStreams> sessions = new ConcurrentHashMap<>();
  private final AtomicLong sessionCount = new AtomicLong(0);

  PathSessions(String path) {
    this.path = path;
  }

  void addSession(String sessionId, SessionStreams streams) {
    sessions.put(sessionId, streams);
    sessionCount.incrementAndGet();
    log.trace("Added session {} to path {}. Total: {}", sessionId, path, sessionCount.get());
  }

  Optional<SessionStreams> removeSession(String sessionId) {
    SessionStreams removed = sessions.remove(sessionId);

    if (removed != null) {
      removed.close(); // Cleanup Sinks
      sessionCount.decrementAndGet();
      log.trace(
          "Removed session {} from path {}. Remaining: {}", sessionId, path, sessionCount.get());
    }

    return Optional.ofNullable(removed);
  }

  Optional<SessionStreams> getSession(String sessionId) {
    return Optional.ofNullable(sessions.get(sessionId));
  }

  Collection<SessionStreams> getAllSessions() {
    return sessions.values();
  }

  long getSessionCount() {
    return sessionCount.get();
  }

  boolean isEmpty() {
    return sessions.isEmpty();
  }

  void shutdown() {
    log.debug("Shutting down path {} with {} sessions", path, sessions.size());
    sessions.values().forEach(SessionStreams::close);
    sessions.clear();
  }
}
