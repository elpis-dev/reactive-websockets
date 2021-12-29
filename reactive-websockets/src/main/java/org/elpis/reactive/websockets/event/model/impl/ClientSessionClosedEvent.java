package org.elpis.reactive.websockets.event.model.impl;

import lombok.Builder;
import org.elpis.reactive.websockets.config.model.ClientSessionCloseInfo;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;

@Builder
public class ClientSessionClosedEvent implements WebSocketEvent<ClientSessionCloseInfo> {
    private final ClientSessionCloseInfo clientSessionCloseInfo;

    @Override
    public ClientSessionCloseInfo payload() {
        return clientSessionCloseInfo;
    }

}
