package org.elpis.reactive.websockets.context;

import org.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import org.elpis.reactive.websockets.EnableReactiveSockets;
import org.elpis.reactive.websockets.config.handler.WebSessionRegistry;
import org.elpis.reactive.websockets.event.impl.ClosedSessionEventSelectorMatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableReactiveSockets
@EnableReactiveSocketSecurity
@Import({
        //Event Matchers
        ClosedSessionEventSelectorMatcher.class,
        WebSessionRegistry.class
})
public class BootStarter {
    public static void main(String[] args) {
        SpringApplication.run(BootStarter.class, args);
    }

    public enum Example {
        VOID
    }
}
