package org.elpis.socket.web.context.resource.security;

import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotations.controller.Outbound;
import org.elpis.reactive.websockets.web.annotations.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotations.request.SocketAuthentication;
import org.reactivestreams.Publisher;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.Map;

@SocketResource("/auth/security")
public class SecurityChainResource implements BasicWebSocketResource {

    @Outbound("/withExtractedAuthentication")
    public Publisher<?> withExtractedAuthentication(@SocketAuthentication final String authentication) {
        return Flux.just(authentication);
    }

    @Outbound("/falseAuthenticationInstance")
    public Publisher<?> withExtractedAuthentication(@SocketAuthentication final Void authentication) {
        return Flux.empty();
    }

    @Outbound("/anonymous")
    public Publisher<?> anonymous(@SocketAuthentication final Principal principal) {
        return Flux.just(Map.of("anonymous", principal == null));
    }

    @Outbound("/principal")
    public Publisher<?> principal(@SocketAuthentication final Principal principal) {
        return Flux.just(principal)
                .filter(principalInstance -> !Authentication.class.isAssignableFrom(principalInstance.getClass()))
                .map(Principal::getName);
    }


}
