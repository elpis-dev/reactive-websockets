package io.github.elpis.reactive.websockets.context.model;

import jakarta.validation.constraints.Email;

public record ValidatedMessage(@Email String username) {}
