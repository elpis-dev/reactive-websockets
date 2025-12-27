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
 * <p>Configuration can be customized via application properties:
 * <ul>
 *   <li>{@code reactive.websockets.registry.maintenance.cleanup.enabled} - Enable/disable cleanup
 *       (default: true)
 *   <li>{@code reactive.websockets.registry.maintenance.cleanup.interval} - Interval in milliseconds
 *       (default: 60000)
 *   <li>{@code reactive.websockets.registry.maintenance.cleanup.initial-delay} - Initial delay in
 *       milliseconds (default: 60000)
 * </ul>
 *
 * @author Phillip J. Fry
 * @since 1.1.0
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
    prefix = "reactive.websockets.registry.maintenance.cleanup",
    name = "enabled",
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
   * Cleanup orphaned sessions at configurable intervals (failsafe).
   *
   * <p>This is a SAFETY NET - proper cleanup should happen in doFinally() handlers. If this finds
   * sessions to clean up, there's likely a bug in lifecycle management.
   *
   * <p>The interval and initial delay can be configured via application properties:
   *
   * <pre>
   * reactive.websockets.registry.maintenance.cleanup.interval=60000
   * reactive.websockets.registry.maintenance.cleanup.initial-delay=60000
   * </pre>
   */
  @Scheduled(
      fixedDelayString =
          "${reactive.websockets.registry.maintenance.cleanup.interval:60000}",
      initialDelayString =
          "${reactive.websockets.registry.maintenance.cleanup.initial-delay:60000}")
  public void cleanupOrphanedSessions() {
    try {
      registry.cleanupOrphanedSessions();
    } catch (Exception e) {
      log.error("Error during orphaned session cleanup", e);
    }
  }
}
