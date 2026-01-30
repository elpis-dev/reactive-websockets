package io.github.elpis.reactive.websockets.flowcontrol.ratelimit;

public record RateLimitWarningDetails(int remainingPermits, int utilizationPercent) {}
