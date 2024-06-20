package org.elpis.reactive.websockets.config.registry;

import org.springframework.lang.Nullable;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.function.BooleanSupplier;

public class WebSocketSessionInfo {

    private String id;

    private BooleanSupplier isOpen = () -> false;

    private String host;

    private int port;

    @Nullable
    private String protocol;

    @Nullable
    private InetSocketAddress remoteAddress;

    @Nullable
    private String path;

    private Mono<CloseStatus> closeStatus = Mono.empty();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BooleanSupplier getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(BooleanSupplier isOpen) {
        this.isOpen = isOpen;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Nullable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(@Nullable String protocol) {
        this.protocol = protocol;
    }

    @Nullable
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(@Nullable InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(@Nullable String path) {
        this.path = path;
    }

     Mono<CloseStatus> getCloseStatus() {
        return closeStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final WebSocketSessionInfo sessionInfo = new WebSocketSessionInfo();

        public Builder id(String id) {
            this.sessionInfo.setId(id);
            return this;
        }

        public Builder isOpen(BooleanSupplier isOpen) {
            this.sessionInfo.setIsOpen(isOpen);
            return this;
        }

        public Builder host(String host) {
            this.sessionInfo.setHost(host);
            return this;
        }

        public Builder port(int port) {
            this.sessionInfo.setPort(port);
            return this;
        }

        public Builder protocol(@Nullable String protocol) {
            this.sessionInfo.setProtocol(protocol);
            return this;
        }

        public Builder remoteAddress(@Nullable InetSocketAddress remoteAddress) {
            this.sessionInfo.setRemoteAddress(remoteAddress);
            return this;
        }

        public Builder path(@Nullable String path) {
            this.sessionInfo.setPath(path);
            return this;
        }

        public Builder closeStatus(@Nullable Mono<CloseStatus> closeStatusMono) {
            this.sessionInfo.closeStatus = closeStatusMono;
            return this;
        }

        public WebSocketSessionInfo build() {
            return this.sessionInfo;
        }
    }
}
