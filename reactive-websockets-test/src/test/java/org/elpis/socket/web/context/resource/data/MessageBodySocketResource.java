package org.elpis.socket.web.context.resource.data;

import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotation.controller.Inbound;
import org.elpis.reactive.websockets.web.annotation.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotation.request.SocketMessageBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

import java.util.List;

@SocketResource("/body")
public class MessageBodySocketResource implements BasicWebSocketResource {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBodySocketResource.class);

    @Inbound("/post")
    public void receiveDefaultMessage(@SocketMessageBody final Flux<WebSocketMessage> webSocketMessageFlux) {
        webSocketMessageFlux.subscribe(message -> LOG.info("Received message: " + message.getPayloadAsText()));
    }

    @Inbound("/post/false")
    public void nonValidFlux(@SocketMessageBody final Flux<String> webSocketMessageFlux) {
        webSocketMessageFlux.subscribe(message -> LOG.info("Received message: " + message));
    }

    @Inbound("/post/not/flux")
    public void notFlux(@SocketMessageBody final List<String> webSocketMessages) {
        webSocketMessages.forEach(message -> LOG.info("Received message: " + message));
    }

}
