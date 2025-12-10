package io.github.elpis.reactive.sample;

import io.github.elpis.reactive.sample.security.CloseStatusHandlers;
import io.github.elpis.reactive.sample.security.SecurityConfiguration;
import io.github.elpis.reactive.sample.socket.config.SampleConfiguration;
import io.github.elpis.reactive.websockets.EnableReactiveSockets;
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
