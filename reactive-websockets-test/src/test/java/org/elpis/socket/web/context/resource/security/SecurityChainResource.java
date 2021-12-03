package org.elpis.socket.web.context.resource.security;

import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotations.controller.Outbound;
import org.elpis.reactive.websockets.web.annotations.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotations.request.SocketAuthentication;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

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

}
