package org.elpis.socket.web;

import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotations.controller.Inbound;
import org.elpis.reactive.websockets.web.annotations.controller.Outbound;
import org.elpis.reactive.websockets.web.annotations.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotations.request.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.time.Duration;
import java.util.Map;

@SocketResource("/ws/chat")
public class ChatWebSocketResource implements BasicWebSocketResource {
    private final static Logger LOG = LoggerFactory.getLogger(ChatWebSocketResource.class);

    @Outbound("/listen/{chatId}")
    public Publisher<?> handleOutbound(@SocketHeader("userName") final String userName,
                                       @SocketPathVariable(value = "chatIds", required = false) final long chatId,
                                       @SocketQueryParam("last") final Integer lastMessages,
                                       @SocketAuthentication final Principal principal) {

        return Flux.interval(Duration.ofSeconds(2))
                .share()
                .map(i -> Map.of("chatId", chatId, "message", i, "userName", userName));
    }

    @Inbound("/listen/{chatId}")
    public void handleInbound(@SocketHeader("userName") final String userName,
                              @SocketPathVariable("chatId") final Long chatId,
                              @SocketQueryParam("last") final Integer lastMessages,
                              @SocketAuthentication final Principal principal,
                              @SocketMessageBody final Flux<WebSocketMessage> messageFlux) {

        messageFlux.subscribe(webSocketMessage ->
                LOG.info("Received message to `{}` on chat `{}` with identity `{}` and message `{}`", userName, chatId,
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
                LOG.info("Received message to `{}` on chat `{}` with identity `{}` and message `{}`", userName, chatId,
                        principal, webSocketMessage.getPayloadAsText()))
                .map(socketMessage -> Map.of("chatId", chatId, "message", socketMessage.getPayloadAsText(), "userName", userName));
    }

}