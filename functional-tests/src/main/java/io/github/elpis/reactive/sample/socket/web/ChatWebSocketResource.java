package io.github.elpis.reactive.sample.socket.web;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;


public class ChatWebSocketResource {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketResource.class);


    @OnMessage(value = "/listen/{chatId}", mode = Mode.SHARED)
    public Publisher<Map<String, Object>> handleOutbound(@RequestHeader("userName") final String userName,
                                                         @PathVariable(value = "chatId", required = false) final Long chatId,
                                                         @RequestParam("last") final Integer lastMessages) {
        return Flux.interval(Duration.ofSeconds(5))
                .share()
                .takeLast(lastMessages)
                .map(i -> Map.of("chatId", chatId, "message", i, "userName", userName));
    }

    @OnMessage(value = "/listen/me/{chatId}", mode = Mode.SHARED)
    public Publisher<Map<String, Object>> handleMulti(@RequestHeader("userName") final String userName,
                                                      @PathVariable("chatId") final Long chatId,
                                                      @RequestBody final Flux<WebSocketMessage> messageFlux) {

        return messageFlux.doOnNext(webSocketMessage ->
                        log.info("Received message to `{}` on chat `{}` with message `{}`", userName, chatId,
                                webSocketMessage.getPayloadAsText()))
                .map(socketMessage -> Map.of("chatId", chatId, "message", socketMessage.getPayloadAsText(), "userName", userName));
    }

    @OnMessage(value = "/listen/chat/{chatId}", mode = Mode.SHARED)
    public Publisher<Object> handleMulti() {
        return Mono.just("abc")
                .delayElement(Duration.ofSeconds(1))
                .then(Mono.error(() -> new IOException("Pfff, that's me!")))
                .onErrorResume(throwable -> Mono.just(CloseStatus.SERVER_ERROR.withReason(throwable.getMessage())));
    }

    @OnMessage(value = "/listen/me", mode = Mode.SHARED)
    public void handle(@RequestBody final Flux<WebSocketMessage> webSocketMessages) {
        webSocketMessages.map(WebSocketMessage::getPayloadAsText)
                .subscribe(log::info);
    }

}
