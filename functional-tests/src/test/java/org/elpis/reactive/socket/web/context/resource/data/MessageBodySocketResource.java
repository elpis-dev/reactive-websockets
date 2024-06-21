package org.elpis.reactive.socket.web.context.resource.data;

import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SocketController("/body")
public class MessageBodySocketResource {
    private static final Logger log = LoggerFactory.getLogger(MessageBodySocketResource.class);

//    @ReceiveMapping("/post")
//    public void receiveDefaultMessage(@SocketMessageBody final Flux<WebSocketMessage> webSocketMessageFlux) {
//        webSocketMessageFlux.subscribe(message -> log.info("Received message: " + message.getPayloadAsText()));
//    }
//
//    @ReceiveMapping("/post/false")
//    public void nonValidFlux(@SocketMessageBody final Flux<String> webSocketMessageFlux) {
//        webSocketMessageFlux.subscribe(message -> log.info("Received message: " + message));
//    }
//
//    @ReceiveMapping("/post/not/flux")
//    public void notFlux(@SocketMessageBody final List<String> webSocketMessages) {
//        webSocketMessages.forEach(message -> log.info("Received message: " + message));
//    }

}
