package org.elpis.socket.web.context;

import org.elpis.reactive.websockets.config.model.WebSocketCloseStatus;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.elpis.reactive.websockets.exception.handler.annotation.CloseStatusHandler;
import org.elpis.reactive.websockets.exception.handler.annotation.SessionCloseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CloseStatusHandler
public class CloseStatusHandlers {
    private static final Logger LOG = LoggerFactory.getLogger(CloseStatusHandlers.class);

    @SessionCloseStatus(WebSocketCloseStatus.ALL)
    public void allCloseEventsHandle(final ClientSessionClosedEvent clientSessionClosedEvent) {
        LOG.info("Got close event from {}", clientSessionClosedEvent);
    }

    @SessionCloseStatus(WebSocketCloseStatus.GOING_AWAY)
    public void goingAwayHandler(final ClientSessionClosedEvent clientSessionClosedEvent) {
        LOG.info("Got close going away event from - {}", clientSessionClosedEvent);
    }

    @SessionCloseStatus(WebSocketCloseStatus.POLICY_VIOLATION)
    public void policyViolationHandler() {
        LOG.info("Got close policy violation event");
    }

    @SessionCloseStatus(code = 4567)
    public void customCodeHandler(final ClientSessionClosedEvent clientSessionClosedEvent) {
        LOG.info("Got custom coded close event from - {}", clientSessionClosedEvent);
    }
}
