package io.github.elpis.reactive.websockets.session;

import static io.github.elpis.reactive.websockets.Constants.NO_SESSIONS;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all sessions for a specific WebSocket path.
 *
 * @since 1.0.0
 */
class ReactiveWebSocketPathSessions {
  private static final Logger log = LoggerFactory.getLogger(ReactiveWebSocketPathSessions.class);

  private final String path;
  private final ConcurrentHashMap<String, SessionStreams> sessions = new ConcurrentHashMap<>();
  private final AtomicLong sessionCount = new AtomicLong(NO_SESSIONS);

  ReactiveWebSocketPathSessions(String path) {
    this.path = path;
  }

  void add(final String sessionId, final SessionStreams streams) {
    sessions.put(sessionId, streams);
    sessionCount.incrementAndGet();

    if (log.isTraceEnabled()) {
      log.trace("Added session {} to path {}. Total: {}", sessionId, path, sessionCount.get());
    }
  }

  Optional<SessionStreams> remove(final String sessionId) {
    final SessionStreams removed = sessions.remove(sessionId);

    if (removed != null) {
      removed.close();
      sessionCount.decrementAndGet();
      if (log.isTraceEnabled()) {
        log.trace(
            "Removed session {} from path {}. Remaining: {}", sessionId, path, sessionCount.get());
      }
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

  void resetSessions() {
    if (log.isDebugEnabled()) {
      log.debug("Shutting down path {} with {} sessions", path, sessions.size());
    }
    sessions.clear();
    sessionCount.set(NO_SESSIONS);
  }
}
