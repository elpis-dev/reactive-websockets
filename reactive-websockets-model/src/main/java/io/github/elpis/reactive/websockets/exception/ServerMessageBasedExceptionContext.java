package io.github.elpis.reactive.websockets.exception;

import io.github.elpis.reactive.websockets.message.ReactiveWebSocketServerMessage;

public interface ServerMessageBasedExceptionContext<
    T, U extends ReactiveWebSocketServerMessage<T>> {
  T getPayload();

  U toServerMessage();
}
