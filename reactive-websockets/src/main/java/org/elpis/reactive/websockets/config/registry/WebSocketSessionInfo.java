package org.elpis.reactive.websockets.config.registry;

import lombok.*;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
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
    private String host;

    @NonNull
    @Getter
    @Setter
    private int port;

    @Nullable
    @Getter
    @Setter
    private String protocol;

    @Nullable
    @Getter
    @Setter
    private InetSocketAddress remoteAddress;

    @Nullable
    @Getter
    @Setter
    private String path;

    @Builder.Default
    @Getter(value = AccessLevel.PACKAGE)
    private final Mono<CloseStatus> closeStatus = Mono.empty();
}
