package io.github.elpis.reactive.websockets.event.model.impl;

import io.github.elpis.reactive.websockets.config.CloseInitiator;
import io.github.elpis.reactive.websockets.config.SessionCloseInfo;
import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;

public class ClientSessionClosedEvent implements WebSocketEvent<SessionCloseInfo> {
    private final SessionCloseInfo sessionCloseInfo;

    public ClientSessionClosedEvent(final SessionCloseInfo sessionCloseInfo) {
        this.sessionCloseInfo = new SessionCloseInfo(sessionCloseInfo);
        this.sessionCloseInfo.setCloseInitiator(CloseInitiator.CLIENT);
    }

    @Override
    public SessionCloseInfo payload() {
        return sessionCloseInfo;
    }

}
