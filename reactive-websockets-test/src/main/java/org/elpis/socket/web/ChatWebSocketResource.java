package org.elpis.socket.web;

import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotation.controller.Inbound;
import org.elpis.reactive.websockets.web.annotation.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotation.request.SocketMessageBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

@SocketResource("/ws/chat")
public class ChatWebSocketResource implements BasicWebSocketResource {
    private final static Logger LOG = LoggerFactory.getLogger(ChatWebSocketResource.class);

//    @Outbound("/listen/{chatId}")
//    public Publisher<?> handleOutbound(@SocketHeader("userName") final String userName,
//                                       @SocketPathVariable(value = "chatIds", required = false) final long chatId,
//                                       @SocketQueryParam("last") final Integer lastMessages,
//                                       @SocketAuthentication final Principal principal) {
//
//        return Flux.interval(Duration.ofSeconds(5))
//                .share()
//                .takeLast(lastMessages)
//                .map(i -> Map.of("chatId", chatId, "message", i, "userName", userName));
//    }
//
//    @Inbound("/listen/{chatId}")
//    public void handleInbound(@SocketHeader("userName") final String userName,
//                              @SocketPathVariable("chatId") final Long chatId,
//                              @SocketQueryParam("last") final Integer lastMessages,
//                              @SocketAuthentication final Principal principal,
//                              @SocketMessageBody final Flux<WebSocketMessage> messageFlux) {
//
//        messageFlux.subscribe(webSocketMessage ->
//                LOG.info("Received message to `{}` on chat `{}` with identity `{}` and message `{}`", userName, chatId,
//                        principal, webSocketMessage.getPayloadAsText()));
//    }
//
//    @Inbound("/listen/me/{chatId}")
//    @Outbound("/listen/me/{chatId}")
//    public Publisher<?> handleMulti(@SocketHeader("userName") final String userName,
//                                    @SocketPathVariable("chatId") final Long chatId,
//                                    @SocketQueryParam("last") final Integer lastMessages,
//                                    @SocketAuthentication final Principal principal,
//                                    @SocketMessageBody final Flux<WebSocketMessage> messageFlux) {
//
//        return messageFlux.doOnNext(webSocketMessage ->
//                LOG.info("Received message to `{}` on chat `{}` with identity `{}` and message `{}`", userName, chatId,
//                        principal, webSocketMessage.getPayloadAsText()))
//                .map(socketMessage -> Map.of("chatId", chatId, "message", socketMessage.getPayloadAsText(), "userName", userName));
//    }

//    @Outbound("/listen/me/{chatId}")
//    public Publisher<?> handleMulti() {
//        return Mono.just("abc")
//                .delayElement(Duration.ofSeconds(1))
//                .then(Mono.error(() -> new IOException("Pfff, that's me!")))
//                .onErrorResume(throwable -> Mono.just(CloseStatus.SERVER_ERROR.withReason(throwable.getMessage())));
//    }

//

    @Inbound("/listen/me")
    public void handle(@SocketMessageBody final Flux<WebSocketMessage> webSocketMessages) {
        webSocketMessages.map(WebSocketMessage::getPayloadAsText)
                .subscribe(System.out::println);
    }

}