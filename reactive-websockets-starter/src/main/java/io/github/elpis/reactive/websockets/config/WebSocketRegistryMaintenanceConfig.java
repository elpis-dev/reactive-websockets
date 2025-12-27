package io.github.elpis.reactive.websockets.config;

import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration for WebSocket session registry maintenance tasks.
 *
 * <p>Provides scheduled cleanup of orphaned sessions as a failsafe mechanism. Normal cleanup should
 * occur via {@code doFinally()} handlers, but this ensures any missed sessions are eventually
 * cleaned up.
 *
 * <p><strong>IMPORTANT</strong>: If this task frequently finds orphaned sessions, it indicates a
 * bug in session lifecycle management that must be investigated.
 *
 * @since 1.1.0
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
    prefix = "reactive.websockets.registry",
    name = "cleanup.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class WebSocketRegistryMaintenanceConfig {

  private static final Logger log =
      LoggerFactory.getLogger(WebSocketRegistryMaintenanceConfig.class);

  private final WebSocketSessionRegistry registry;

  public WebSocketRegistryMaintenanceConfig(WebSocketSessionRegistry registry) {
    this.registry = registry;
  }

  /**
   * Cleanup orphaned sessions every minute (failsafe).
   *
   * <p>This is a SAFETY NET - proper cleanup should happen in doFinally() handlers. If this finds
   * sessions to clean up, there's likely a bug in lifecycle management.
   */
  @Scheduled(fixedDelay = 60_000) // Every 60 seconds
  public void cleanupOrphanedSessions() {
    try {
      registry.cleanupOrphanedSessions();
    } catch (Exception e) {
      log.error("Error during orphaned session cleanup", e);
    }
  }

  /** Log registry metrics every 5 minutes (optional monitoring). */
  @Scheduled(fixedDelay = 300_000)
  public void logRegistryMetrics() {
    long totalSessions = registry.getTotalSessionCount();

    if (totalSessions > 0) {
      log.info(
          "WebSocket Registry Metrics: {} total sessions across {} paths",
          totalSessions,
          registry.getAllPaths().size());

      if (log.isDebugEnabled()) {
        registry
            .getAllPaths()
            .forEach(
                path -> log.debug("  Path {}: {} sessions", path, registry.getSessionCount(path)));
      }
    }
  }
}
