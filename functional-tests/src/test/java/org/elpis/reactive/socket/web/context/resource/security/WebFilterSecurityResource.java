package org.elpis.reactive.socket.web.context.resource.security;

import org.elpis.reactive.websockets.web.annotation.SocketController;

@SocketController("/auth/filter")
public class WebFilterSecurityResource {

//    @SendMapping("/withPrincipal")
//    public Publisher<?> withPrincipal(@SocketAuthentication final Principal principal) {
//        return Flux.just(principal)
//                .map(Principal::getName);
//    }
//
//    @SendMapping("/withAuthentication")
//    public Publisher<?> withAuthentication(@SocketAuthentication final Authentication authentication) {
//        return Flux.just(authentication)
//                .map(auth -> Map.of(TestConstants.PRINCIPAL, authentication.getName()));
//    }
//
//    @SendMapping("/withWebSocketPrincipal")
//    public Publisher<?> withWebSocketPrincipal(@SocketAuthentication final Principal principal) {
//        return Flux.just(principal)
//                .cast(WebSocketPrincipal.class)
//                .map(WebSocketPrincipal::getAuthentication);
//    }
//
//    @SendMapping("/withExtractedAuthentication")
//    public Publisher<?> withExtractedAuthentication(@SocketAuthentication final String authentication) {
//        return Flux.just(authentication);
//    }
//
//    @SendMapping("/withDetailsFromAuthentication")
//    public Publisher<?> withDetailsFromAuthentication(@SocketAuthentication final String authentication) {
//        return Flux.just(authentication);
//    }

}
