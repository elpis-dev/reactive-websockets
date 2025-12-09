package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@MessageEndpoint("/body")
public class MessageBodySocketResource {
    private static final Logger log = LoggerFactory.getLogger(MessageBodySocketResource.class);

    @OnMessage(value = "/post", mode = Mode.SHARED)
    public void receiveDefaultMessage(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
        webSocketMessageFlux.subscribe(message -> log.info("Received message: " + message.getPayloadAsText()));
    }

    @OnMessage(value = "/post/binary", mode = Mode.SHARED)
    public Flux<byte[]> sendBinaryMessage() {
        return Flux.just("Binary".getBytes());
    }

    @OnMessage(value = "/post/stream", mode = Mode.SHARED)
    public Mono<InputStream> sendStreamMessage() {
        return Mono.just(new ByteArrayInputStream("Stream".getBytes()));
    }

}
