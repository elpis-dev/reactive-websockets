package org.elpis.reactive.websockets.web.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.lang.Nullable;

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

    @NonNull
    @Builder.Default
    private Runnable close = () -> {};
}
