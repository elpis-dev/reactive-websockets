package org.elpis.reactive.websockets.web.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.reactivestreams.Publisher;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.function.Supplier;

@Getter
@Setter
@Builder(toBuilder = true)
public class WebSocketSessionInfo {

    @NonNull
    private String id;

    @NonNull
    @Builder.Default
    private Supplier<Boolean> isOpen = () -> false;

    @NonNull
    private URI uri;

    @Nullable
    private String protocol;

    @Nullable
    private InetSocketAddress remoteAddress;
}
