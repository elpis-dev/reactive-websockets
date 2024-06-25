package org.elpis.reactive.socket.web.context;

import org.elpis.reactive.websockets.EnableReactiveSockets;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;
import org.elpis.reactive.websockets.event.impl.ClosedSessionEventSelectorMatcher;
import org.elpis.reactive.websockets.mapper.JsonMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableReactiveSockets
@Import({
        //Event Matchers
        ClosedSessionEventSelectorMatcher.class,

        WebSessionRegistry.class,
        JsonMapper.class
})
public class BootStarter {
    public static void main(String[] args) {
        SpringApplication.run(BootStarter.class, args);
    }

    public enum Example {
        VOID
    }
}
