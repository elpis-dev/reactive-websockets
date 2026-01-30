package io.github.elpis.reactive.websockets.flowcontrol.ratelimit;

import io.github.elpis.reactive.websockets.config.MessageCode;
import io.github.elpis.reactive.websockets.config.MessageType;
import io.github.elpis.reactive.websockets.message.ReactiveWebSocketServerMessage;

public class RateLimitWarningServerMessage
    implements ReactiveWebSocketServerMessage<RateLimitWarningDetails> {
  private final RateLimitWarningDetails details;

  public RateLimitWarningServerMessage(final RateLimitWarningDetails details) {
    this.details = details;
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.SYSTEM_WARNING;
  }

  @Override
  public MessageCode getMessageCode() {
    return MessageCode.APPROACHING_RATE_LIMIT;
  }

  @Override
  public RateLimitWarningDetails getMessage() {
    return this.details;
  }
}
