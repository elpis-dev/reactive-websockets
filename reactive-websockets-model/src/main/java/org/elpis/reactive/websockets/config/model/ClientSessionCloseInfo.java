package org.elpis.reactive.websockets.config.model;


import org.elpis.reactive.websockets.config.registry.WebSocketSessionInfo;
import org.springframework.web.reactive.socket.CloseStatus;

public class ClientSessionCloseInfo {
    private WebSocketSessionInfo sessionInfo;
    private CloseStatus closeStatus;

    public WebSocketSessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(WebSocketSessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public CloseStatus getCloseStatus() {
        return closeStatus;
    }

    public void setCloseStatus(CloseStatus closeStatus) {
        this.closeStatus = closeStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ClientSessionCloseInfo clientSessionCloseInfo = new ClientSessionCloseInfo();

        public Builder sessionInfo(WebSocketSessionInfo sessionInfo) {
            this.clientSessionCloseInfo.setSessionInfo(sessionInfo);
            return this;
        }

        public Builder closeStatus(CloseStatus closeStatus) {
            this.clientSessionCloseInfo.setCloseStatus(closeStatus);
            return this;
        }

        public ClientSessionCloseInfo build() {
            return this.clientSessionCloseInfo;
        }
    }
}
