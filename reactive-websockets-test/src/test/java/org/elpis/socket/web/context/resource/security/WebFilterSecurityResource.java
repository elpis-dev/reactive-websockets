package org.elpis.socket.web.context.resource.security;

import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotations.controller.Outbound;
import org.elpis.reactive.websockets.web.annotations.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotations.request.SocketAuthentication;
import org.elpis.socket.web.context.security.model.TestConstants;
import org.reactivestreams.Publisher;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.Map;

@SocketResource("/auth/filter")
public class WebFilterSecurityResource implements BasicWebSocketResource {

    @Outbound("/withPrincipal")
    public Publisher<?> withPrincipal(@SocketAuthentication final Principal principal) {
        return Flux.just(principal)
                .map(Principal::getName);
    }

    @Outbound("/withAuthentication")
    public Publisher<?> withAuthentication(@SocketAuthentication final Authentication authentication) {
        return Flux.just(authentication)
                .map(auth -> Map.of(TestConstants.PRINCIPAL, authentication.getName()));
    }

    @Outbound("/withWebSocketPrincipal")
    public Publisher<?> withWebSocketPrincipal(@SocketAuthentication final Principal principal) {
        return Flux.just(principal)
                .cast(WebSocketPrincipal.class)
                .map(WebSocketPrincipal::getAuthentication);
    }

    @Outbound("/withExtractedAuthentication")
    public Publisher<?> withExtractedAuthentication(@SocketAuthentication final String authentication) {
        return Flux.just(authentication);
    }

}
