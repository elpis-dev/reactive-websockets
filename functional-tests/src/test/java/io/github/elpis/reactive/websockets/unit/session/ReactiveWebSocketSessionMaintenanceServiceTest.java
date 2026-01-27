package io.github.elpis.reactive.websockets.unit.session;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionMaintenanceService;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReactiveWebSocketSessionMaintenanceServiceTest {
  private static final String DEFAULT = "/default";
  private static final String SESSION_ID = UUID.randomUUID().toString();

  @Mock ReactiveWebSocketSessionRegistry registry;
  @Mock SessionStreams streams;
  @Mock ReactiveWebSocketSession session;

  ReactiveWebSocketSessionMaintenanceService maintenanceService;

  @BeforeEach
  void setup() {
    this.maintenanceService = new ReactiveWebSocketSessionMaintenanceService(this.registry);
  }

  @Test
  void testCleanupOrphanedSessions() {
    // when
    when(this.registry.getAllPaths()).thenReturn(List.of(DEFAULT));
    when(this.registry.getAllSessions(DEFAULT)).thenReturn(List.of(this.streams));
    when(this.streams.metadata()).thenReturn(this.session);
    when(this.session.isOpen()).thenReturn(false);
    when(this.session.getSessionId()).thenReturn(SESSION_ID);

    // test
    this.maintenanceService.cleanupOrphanedSessions();

    // verify
    verify(this.registry).unregister(DEFAULT, SESSION_ID);
  }
}
