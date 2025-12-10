package io.github.elpis.reactive.websockets.context.security;

import io.github.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import io.github.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@EnableReactiveSocketSecurity
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile(SecurityProfiles.FULL)
public class TestSecurityConfiguration {}
