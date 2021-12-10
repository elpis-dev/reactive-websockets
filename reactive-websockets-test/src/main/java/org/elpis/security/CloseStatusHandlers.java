package org.elpis.security;

import org.elpis.reactive.websockets.config.model.WebSocketCloseStatus;
import org.elpis.reactive.websockets.event.annotation.EventSelector;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.elpis.reactive.websockets.exception.handler.annotation.CloseStatusHandler;
import org.elpis.reactive.websockets.exception.handler.annotation.SessionCloseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CloseStatusHandler
public class CloseStatusHandlers {
    private static final Logger LOG = LoggerFactory.getLogger(CloseStatusHandlers.class);

    @SessionCloseStatus(WebSocketCloseStatus.ALL)
    @EventSelector("sessionInfo.path.contains('/listen/me')")
    public void allCloseEventsHandle(final ClientSessionClosedEvent clientSessionClosedEvent) {
        LOG.info("Got close event from '/listen/me' - {}", clientSessionClosedEvent);
    }

    @SessionCloseStatus(WebSocketCloseStatus.GOING_AWAY)
    @EventSelector("sessionInfo.id eq '12345'")
    public void goingAwayHandler(final ClientSessionClosedEvent clientSessionClosedEvent) {
        LOG.info("Got close event from session with id '12345' - {}", clientSessionClosedEvent);
    }
}
