package io.github.elpis.reactive.websockets.context;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BootStarter {
  public static void main(String[] args) {
    SpringApplication.run(BootStarter.class, args);
  }

  public enum Example {
    VOID
  }
}
