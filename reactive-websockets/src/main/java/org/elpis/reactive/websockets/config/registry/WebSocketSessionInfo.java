package org.elpis.reactive.websockets.config.registry;

import lombok.*;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.function.BooleanSupplier;

@Builder(toBuilder = true)
public class WebSocketSessionInfo {

    @NonNull
    @Getter
    @Setter
    private String id;

    @NonNull
    @Builder.Default
    @Getter
    @Setter
    private BooleanSupplier isOpen = () -> false;

    @NonNull
    @Getter
    @Setter
    private URI uri;

    @Nullable
    @Getter
    @Setter
    private String protocol;

    @Nullable
    @Getter
    @Setter
    private InetSocketAddress remoteAddress;

    @Builder.Default
    @Getter(value = AccessLevel.PACKAGE)
    private final Mono<CloseStatus> closeStatus = Mono.empty();
}
