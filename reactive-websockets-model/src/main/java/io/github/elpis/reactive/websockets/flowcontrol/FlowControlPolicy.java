package io.github.elpis.reactive.websockets.flowcontrol;

import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.util.TriFunction;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

/**
 * Represents a flow control policy for WebSocket communication.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public interface FlowControlPolicy
    extends TriFunction<
        String, WebSocketSessionContext, Flux<WebSocketMessage>, Flux<WebSocketMessage>> {

  /**
   * Gets the placement of this flow control policy (input or output).
   *
   * @return the flow control placement
   */
  FlowControlPlacement placement();
}
