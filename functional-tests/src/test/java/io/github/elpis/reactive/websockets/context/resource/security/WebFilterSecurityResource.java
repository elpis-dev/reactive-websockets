package io.github.elpis.reactive.websockets.context.resource.security;

import io.github.elpis.reactive.websockets.context.security.model.TestConstants;
import io.github.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.reactivestreams.Publisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.Map;

import static io.github.elpis.reactive.websockets.config.Mode.BROADCAST;

@MessageEndpoint("/auth/filter")
public class WebFilterSecurityResource {

    @OnMessage(value = "/withPrincipal", mode = BROADCAST)
    public Publisher<?> withPrincipal(@AuthenticationPrincipal final Principal principal) {
        return Flux.just(principal)
                .map(Principal::getName);
    }

    @OnMessage(value = "/withAuthentication", mode = BROADCAST)
    public Publisher<?> withAuthentication(@AuthenticationPrincipal final Authentication authentication) {
        return Flux.just(authentication)
                .map(auth -> Map.of(TestConstants.PRINCIPAL, authentication.getName()));
    }

    @OnMessage(value = "/withWebSocketPrincipal", mode = BROADCAST)
    public Publisher<?> withWebSocketPrincipal(@AuthenticationPrincipal final Principal principal) {
        return Flux.just(principal)
                .cast(WebSocketPrincipal.class)
                .map(WebSocketPrincipal::getAuthentication);
    }

    @OnMessage(value = "/withExtractedAuthentication", mode = BROADCAST)
    public Publisher<?> withExtractedAuthentication(@AuthenticationPrincipal final WebSocketPrincipal<String> authentication) {
        return Flux.just(authentication.getAuthentication());
    }

    @OnMessage(value = "/withExpressionPrincipal", mode = BROADCAST)
    public Publisher<?> withExpressionPrincipal(@AuthenticationPrincipal(expression = "principal.name") final String principalName) {
        return Flux.just(principalName);
    }

}
