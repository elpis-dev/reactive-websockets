package io.github.elpis.reactive.websockets.util;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;

public class MessageUtils {
  private static final DataBufferFactory BUFFER_FACTORY = new DefaultDataBufferFactory();

  public static WebSocketMessage textMessage(final String message) {
    byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = BUFFER_FACTORY.wrap(bytes);
    return new WebSocketMessage(WebSocketMessage.Type.TEXT, buffer);
  }
}
