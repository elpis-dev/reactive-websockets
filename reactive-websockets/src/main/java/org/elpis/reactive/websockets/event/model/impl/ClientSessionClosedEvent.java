package org.elpis.reactive.websockets.event.model.impl;

import org.elpis.reactive.websockets.config.model.ClientSessionCloseInfo;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;

public class ClientSessionClosedEvent implements WebSocketEvent<ClientSessionCloseInfo> {
    private ClientSessionCloseInfo clientSessionCloseInfo;

    @Override
    public ClientSessionCloseInfo payload() {
        return clientSessionCloseInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ClientSessionClosedEvent closedEvent = new ClientSessionClosedEvent();

        public Builder clientSessionCloseInfo(ClientSessionCloseInfo clientSessionCloseInfo) {
            this.closedEvent.clientSessionCloseInfo = clientSessionCloseInfo;
            return this;
        }

        public ClientSessionClosedEvent build() {
            return this.closedEvent;
        }
    }

}
