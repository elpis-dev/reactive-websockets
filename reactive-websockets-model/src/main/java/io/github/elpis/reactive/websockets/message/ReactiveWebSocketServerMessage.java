package io.github.elpis.reactive.websockets.message;

import io.github.elpis.reactive.websockets.config.MessageCode;
import io.github.elpis.reactive.websockets.config.MessageType;

public interface ReactiveWebSocketServerMessage<T> {
  MessageType getMessageType();

  MessageCode getMessageCode();

  T getMessage();
}
