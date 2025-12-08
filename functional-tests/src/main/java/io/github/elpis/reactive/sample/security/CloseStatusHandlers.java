package io.github.elpis.reactive.sample.security;

import io.github.elpis.reactive.websockets.config.WebSocketCloseStatus;
import io.github.elpis.reactive.websockets.event.annotation.EventSelector;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.annotation.CloseStatusHandler;
import io.github.elpis.reactive.websockets.event.annotation.SessionCloseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CloseStatusHandler
public class CloseStatusHandlers {
    private static final Logger log = LoggerFactory.getLogger(CloseStatusHandlers.class);

    @SessionCloseStatus(WebSocketCloseStatus.ALL)

    public void allCloseEventsHandle(final ClientSessionClosedEvent clientSessionClosedEvent) {
        log.info("Got close event from '/listen/me' - {}", clientSessionClosedEvent);
    }

    @SessionCloseStatus(WebSocketCloseStatus.GOING_AWAY)
    @EventSelector("session.sessionId eq '12345'")
    public void goingAwayHandler(final ClientSessionClosedEvent clientSessionClosedEvent) {
        log.info("Got close event from session with id '12345' - {}", clientSessionClosedEvent);
    }
}
