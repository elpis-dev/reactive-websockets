package org.elpis.reactive.sample;

import org.elpis.reactive.sample.security.CloseStatusHandlers;
import org.elpis.reactive.sample.security.SecurityConfiguration;
import org.elpis.reactive.sample.socket.config.SampleConfiguration;
import org.elpis.reactive.websockets.EnableReactiveSockets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableReactiveSockets
@Import({SampleConfiguration.class, SecurityConfiguration.class, CloseStatusHandlers.class})
public class BootStarter {
    public static void main(String[] args) {
        SpringApplication.run(BootStarter.class, args);
    }
}
