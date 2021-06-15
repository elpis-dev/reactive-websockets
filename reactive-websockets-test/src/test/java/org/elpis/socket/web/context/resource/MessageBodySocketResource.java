package org.elpis.socket.web.context.resource;

import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotations.controller.Inbound;
import org.elpis.reactive.websockets.web.annotations.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotations.request.SocketMessageBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

import java.util.List;

@SocketResource("/body")
public class MessageBodySocketResource implements BasicWebSocketResource {
    private static final Logger log = LoggerFactory.getLogger(MessageBodySocketResource.class);

    @Inbound("/post")
    public void receiveDefaultMessage(@SocketMessageBody final Flux<WebSocketMessage> webSocketMessageFlux) {
        webSocketMessageFlux.subscribe(message -> log.info("Received message: " + message.getPayloadAsText()));
    }

    @Inbound("/post/false")
    public void nonValidFlux(@SocketMessageBody final Flux<String> webSocketMessageFlux) {
        webSocketMessageFlux.subscribe(message -> log.info("Received message: " + message));
    }

    @Inbound("/post/not/flux")
    public void notFlux(@SocketMessageBody final List<String> webSocketMessages) {
        webSocketMessages.forEach(message -> log.info("Received message: " + message));
    }

}