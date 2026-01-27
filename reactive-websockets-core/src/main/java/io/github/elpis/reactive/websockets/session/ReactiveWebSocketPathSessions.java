package io.github.elpis.reactive.websockets.session;

import static io.github.elpis.reactive.websockets.Constants.NO_SESSIONS;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages WebSocket sessions for a specific endpoint path.
 *
 * <p>This class maintains a mapping of session IDs to their corresponding {@link SessionStreams},
 * allowing for efficient addition, removal, and retrieval of sessions associated with a given path.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
final class ReactiveWebSocketPathSessions {
  private static final Logger log = LoggerFactory.getLogger(ReactiveWebSocketPathSessions.class);

  private final String path;
  private final ConcurrentHashMap<String, SessionStreams> sessions = new ConcurrentHashMap<>();
  private final AtomicLong sessionCount = new AtomicLong(NO_SESSIONS);

  ReactiveWebSocketPathSessions(String path) {
    this.path = path;
  }

  /**
   * Adds a new session to the path.
   *
   * @param sessionId the unique identifier of the session
   * @param streams the session streams associated with the session
   */
  void add(final String sessionId, final SessionStreams streams) {
    sessions.put(sessionId, streams);
    sessionCount.incrementAndGet();

    if (log.isTraceEnabled()) {
      log.trace("Added session {} to path {}. Total: {}", sessionId, path, sessionCount.get());
    }
  }

  /**
   * Removes a session from the path.
   *
   * @param sessionId the unique identifier of the session to remove
   * @return an Optional containing the removed SessionStreams if it existed, otherwise empty
   */
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

  /**
   * Retrieves a session by its ID.
   *
   * @param sessionId the unique identifier of the session
   * @return an Optional containing the SessionStreams if found, otherwise empty
   */
  Optional<SessionStreams> getSession(String sessionId) {
    return Optional.ofNullable(sessions.get(sessionId));
  }

  /**
   * Retrieves all sessions associated with the path.
   *
   * @return a collection of all SessionStreams
   */
  Collection<SessionStreams> getAllSessions() {
    return sessions.values();
  }

  /**
   * Gets the current count of active sessions for the path.
   *
   * @return the number of active sessions
   */
  long getSessionCount() {
    return sessionCount.get();
  }

  /**
   * Checks if there are no active sessions for the path.
   *
   * @return true if there are no sessions, false otherwise
   */
  boolean isEmpty() {
    return sessions.isEmpty();
  }

  /** Resets all sessions for the path, clearing the session map and resetting the count. */
  void resetSessions() {
    if (log.isDebugEnabled()) {
      log.debug("Shutting down path {} with {} sessions", path, sessions.size());
    }
    sessions.clear();
    sessionCount.set(NO_SESSIONS);
  }
}
