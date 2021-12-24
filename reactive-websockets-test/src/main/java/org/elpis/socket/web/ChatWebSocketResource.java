package org.elpis.socket.web;

import org.elpis.reactive.websockets.web.annotation.controller.Inbound;
import org.elpis.reactive.websockets.web.annotation.controller.Outbound;
import org.elpis.reactive.websockets.web.annotation.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotation.request.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.Principal;
import java.time.Duration;
import java.util.Map;

@SocketResource("/ws/chat")
public class ChatWebSocketResource {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketResource.class);

    @Outbound("/listen/{chatId}")
    public Publisher<?> handleOutbound(@SocketHeader("userName") final String userName,
                                       @SocketPathVariable(value = "chatIds", required = false) final long chatId,
                                       @SocketQueryParam("last") final Integer lastMessages,
                                       @SocketAuthentication final Principal principal) {

        return Flux.interval(Duration.ofSeconds(5))
                .share()
                .takeLast(lastMessages)
                .map(i -> Map.of("chatId", chatId, "message", i, "userName", userName));
    }

    @Inbound("/listen/{chatId}")
    public void handleInbound(@SocketHeader("userName") final String userName,
                              @SocketPathVariable("chatId") final Long chatId,
                              @SocketQueryParam("last") final Integer lastMessages,
                              @SocketAuthentication final Principal principal,
                              @SocketMessageBody final Flux<WebSocketMessage> messageFlux) {

        messageFlux.subscribe(webSocketMessage ->
                log.info("Received message to `{}` on chat `{}` with identity `{}` and message `{}`", userName, chatId,
                        principal, webSocketMessage.getPayloadAsText()));
    }

    @Inbound("/listen/me/{chatId}")
    @Outbound("/listen/me/{chatId}")
    public Publisher<?> handleMulti(@SocketHeader("userName") final String userName,
                                    @SocketPathVariable("chatId") final Long chatId,
                                    @SocketQueryParam("last") final Integer lastMessages,
                                    @SocketAuthentication final Principal principal,
                                    @SocketMessageBody final Flux<WebSocketMessage> messageFlux) {

        return messageFlux.doOnNext(webSocketMessage ->
                log.info("Received message to `{}` on chat `{}` with identity `{}` and message `{}`", userName, chatId,
                        principal, webSocketMessage.getPayloadAsText()))
                .map(socketMessage -> Map.of("chatId", chatId, "message", socketMessage.getPayloadAsText(), "userName", userName));
    }

    @Outbound("/listen/chat/{chatId}")
    public Publisher<?> handleMulti() {
        return Mono.just("abc")
                .delayElement(Duration.ofSeconds(1))
                .then(Mono.error(() -> new IOException("Pfff, that's me!")))
                .onErrorResume(throwable -> Mono.just(CloseStatus.SERVER_ERROR.withReason(throwable.getMessage())));
    }

    @Inbound("/listen/me")
    public void handle(@SocketMessageBody final Flux<WebSocketMessage> webSocketMessages) {
        webSocketMessages.map(WebSocketMessage::getPayloadAsText)
                .subscribe(log::info);
    }

}
