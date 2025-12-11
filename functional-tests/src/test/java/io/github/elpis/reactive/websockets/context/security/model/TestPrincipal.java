package io.github.elpis.reactive.websockets.context.security.model;

import java.security.Principal;

public class TestPrincipal implements Principal {

  @Override
  public String getName() {
    return TestPrincipal.class.getName();
  }
}
