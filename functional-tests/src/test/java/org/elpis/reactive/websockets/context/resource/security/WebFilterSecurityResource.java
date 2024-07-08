package org.elpis.reactive.websockets.context.resource.security;

import org.elpis.reactive.websockets.context.security.model.TestConstants;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.reactivestreams.Publisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.Map;

import static org.elpis.reactive.websockets.config.Mode.SHARED;

@SocketController("/auth/filter")
public class WebFilterSecurityResource {

    @SocketMapping(value = "/withPrincipal", mode = SHARED)
    public Publisher<?> withPrincipal(@AuthenticationPrincipal final Principal principal) {
        return Flux.just(principal)
                .map(Principal::getName);
    }

    @SocketMapping(value = "/withAuthentication", mode = SHARED)
    public Publisher<?> withAuthentication(@AuthenticationPrincipal final Authentication authentication) {
        return Flux.just(authentication)
                .map(auth -> Map.of(TestConstants.PRINCIPAL, authentication.getName()));
    }

    @SocketMapping(value = "/withWebSocketPrincipal", mode = SHARED)
    public Publisher<?> withWebSocketPrincipal(@AuthenticationPrincipal final Principal principal) {
        return Flux.just(principal)
                .cast(WebSocketPrincipal.class)
                .map(WebSocketPrincipal::getAuthentication);
    }

    @SocketMapping(value = "/withExtractedAuthentication", mode = SHARED)
    public Publisher<?> withExtractedAuthentication(@AuthenticationPrincipal final WebSocketPrincipal<String> authentication) {
        return Flux.just(authentication.getAuthentication());
    }

}
