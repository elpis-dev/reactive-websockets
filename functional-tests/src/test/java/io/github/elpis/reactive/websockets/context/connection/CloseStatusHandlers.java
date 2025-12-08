package io.github.elpis.reactive.websockets.context.connection;

import io.github.elpis.reactive.websockets.config.WebSocketCloseStatus;
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
        log.info("Got close event from {}", clientSessionClosedEvent);
    }

    @SessionCloseStatus(WebSocketCloseStatus.GOING_AWAY)
    public void goingAwayHandler(final ClientSessionClosedEvent clientSessionClosedEvent) {
        log.info("Got close going away event from - {}", clientSessionClosedEvent);
    }

    @SessionCloseStatus(WebSocketCloseStatus.POLICY_VIOLATION)
    public void policyViolationHandler() {
        log.info("Got close policy violation event");
    }

    @SessionCloseStatus(code = 4567)
    public void customCodeHandler(final ClientSessionClosedEvent clientSessionClosedEvent) {
        log.info("Got custom coded close event from - {}", clientSessionClosedEvent);
    }
}
