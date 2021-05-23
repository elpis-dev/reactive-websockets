package org.elpis;

import org.elpis.reactive.websockets.EnableReactiveSockets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableReactiveSockets
public class BootStarter {
    public static void main(String[] args) {
        SpringApplication.run(BootStarter.class, args);
    }
}
