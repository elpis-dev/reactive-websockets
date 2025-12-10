package io.github.elpis.reactive.websockets.config;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import org.springframework.web.reactive.socket.CloseStatus;

public class SessionCloseInfo {
  private ReactiveWebSocketSession session;
  private CloseStatus closeStatus;
  private CloseInitiator closeInitiator;

  public SessionCloseInfo() {}

  public SessionCloseInfo(SessionCloseInfo sessionCloseInfo) {
    this.session = sessionCloseInfo.session;
    this.closeStatus = sessionCloseInfo.closeStatus;
    this.closeInitiator = sessionCloseInfo.closeInitiator;
  }

  public ReactiveWebSocketSession getSession() {
    return session;
  }

  public void setSession(ReactiveWebSocketSession session) {
    this.session = session;
  }

  public CloseStatus getCloseStatus() {
    return closeStatus;
  }

  public void setCloseStatus(CloseStatus closeStatus) {
    this.closeStatus = closeStatus;
  }

  public void setCloseInitiator(CloseInitiator closeInitiator) {
    this.closeInitiator = closeInitiator;
  }

  public CloseInitiator getCloseInitiator() {
    return closeInitiator;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final SessionCloseInfo sessionCloseInfo = new SessionCloseInfo();

    public Builder session(ReactiveWebSocketSession sessionInfo) {
      this.sessionCloseInfo.setSession(sessionInfo);
      return this;
    }

    public Builder closeStatus(CloseStatus closeStatus) {
      this.sessionCloseInfo.setCloseStatus(closeStatus);
      return this;
    }

    public Builder closeInitiator(CloseInitiator initiator) {
      this.sessionCloseInfo.setCloseInitiator(initiator);
      return this;
    }

    public SessionCloseInfo build() {
      return this.sessionCloseInfo;
    }
  }
}
