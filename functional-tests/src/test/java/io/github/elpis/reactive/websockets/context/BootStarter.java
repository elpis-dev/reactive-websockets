package io.github.elpis.reactive.websockets.context;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Boot Starter for functional tests. Excludes resource packages to make per-test resource imports.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.REGEX,
          pattern = "io\\.github\\.elpis\\.reactive\\.websockets\\.resource\\..*")
    })
public class BootStarter {
  public static void main(String[] args) {
    SpringApplication.run(BootStarter.class, args);
  }

  public enum Example {
    VOID
  }
}
