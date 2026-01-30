package io.github.elpis.reactive.websockets.exception.flowcontrol;

import io.github.elpis.reactive.websockets.exception.ServerMessageBasedExceptionContext;
import io.github.elpis.reactive.websockets.flowcontrol.ratelimit.RateLimitWarningDetails;
import io.github.elpis.reactive.websockets.flowcontrol.ratelimit.RateLimitWarningServerMessage;

public final class RateLimitWarningException extends RuntimeException
    implements ServerMessageBasedExceptionContext<
        RateLimitWarningDetails, RateLimitWarningServerMessage> {
  private final RateLimitWarningDetails rateLimitWarningDetails;

  public RateLimitWarningException(
      final String message, final int remainingPermits, int utilizationPercent) {
    super(message);
    this.rateLimitWarningDetails =
        new RateLimitWarningDetails(remainingPermits, utilizationPercent);
  }

  public RateLimitWarningException(
      final String message, final RateLimitWarningDetails rateLimitWarningDetails) {
    super(message);
    this.rateLimitWarningDetails = rateLimitWarningDetails;
  }

  @Override
  public RateLimitWarningDetails getPayload() {
    return this.rateLimitWarningDetails;
  }

  @Override
  public RateLimitWarningServerMessage toServerMessage() {
    return new RateLimitWarningServerMessage(this.rateLimitWarningDetails);
  }
}
