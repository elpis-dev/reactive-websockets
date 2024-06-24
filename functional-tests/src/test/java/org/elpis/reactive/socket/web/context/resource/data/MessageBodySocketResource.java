package org.elpis.reactive.socket.web.context.resource.data;

import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

@SocketController("/body")
public class MessageBodySocketResource {
    private static final Logger log = LoggerFactory.getLogger(MessageBodySocketResource.class);

    @SocketMapping(value = "/post", mode = Mode.SHARED)
    public void receiveDefaultMessage(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
        webSocketMessageFlux.subscribe(message -> log.info("Received message: " + message.getPayloadAsText()));
    }

}
