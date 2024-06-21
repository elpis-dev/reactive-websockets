package org.elpis.reactive.socket.web.context.resource.security;

import org.elpis.reactive.websockets.web.annotation.SocketController;

@SocketController("/auth/security")
public class SecurityChainResource {

//    @SendMapping("/withExtractedAuthentication")
//    public Publisher<?> withExtractedAuthentication(@SocketAuthentication final String authentication) {
//        return Flux.just(authentication);
//    }
//
//    @SendMapping("/falseAuthenticationInstance")
//    public Publisher<?> withExtractedAuthentication(@SocketAuthentication final Void authentication) {
//        return Flux.empty();
//    }
//
//    @SendMapping("/anonymous")
//    public Publisher<?> anonymous(@SocketAuthentication final Principal principal) {
//        return Flux.just(Map.of("anonymous", principal == null));
//    }
//
//    @SendMapping("/principal")
//    public Publisher<?> principal(@SocketAuthentication final Principal principal) {
//        return Flux.just(principal)
//                .filter(principalInstance -> !Authentication.class.isAssignableFrom(principalInstance.getClass()))
//                .map(Principal::getName);
//    }


}
