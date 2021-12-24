package org.elpis.reactive.websockets.config.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Data
@Builder(toBuilder = true)
public class WebSocketSessionContext {
    @NonNull
    @Builder.Default
    private Map<String, String> pathParameters = new HashMap<>();

    @NonNull
    @Builder.Default
    private MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();

    @NonNull
    @Builder.Default
    private HttpHeaders headers = new HttpHeaders();

    @NonNull
    @Builder.Default
    private Principal authentication = new Anonymous();

    @NonNull
    @Builder.Default
    private Supplier<Flux<WebSocketMessage>> messageStream = Flux::never;

    @NonNull
    private String sessionId;

    private boolean inbound;

    private boolean outbound;
}
