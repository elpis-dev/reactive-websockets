package io.github.elpis.reactive.websockets.config.session;

import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketSessionConfiguration {

    @Bean
    public WebSocketSessionRegistry webSocketSessionRegistry() {
        return new WebSocketSessionRegistry();
    }

}
