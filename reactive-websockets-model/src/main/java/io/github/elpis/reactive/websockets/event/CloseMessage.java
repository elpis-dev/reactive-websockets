package io.github.elpis.reactive.websockets.event;

import org.springframework.web.reactive.socket.CloseStatus;

public class CloseMessage {
    private String sessionId;
    private CloseStatus closeStatus;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public CloseStatus getCloseStatus() {
        return closeStatus;
    }

    public void setCloseStatus(CloseStatus closeStatus) {
        this.closeStatus = closeStatus;
    }
}
