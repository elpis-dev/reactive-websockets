package io.github.elpis.reactive.websockets.config.flowcontrol;

import io.github.elpis.reactive.websockets.web.annotation.Backpressure;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.webflux.reactive.websockets.flow-control")
public final class ReactiveWebSocketFlowControlProperties {
  private BackpressureProperties backpressure = new BackpressureProperties();
  private RateLimitProperties rateLimit = new RateLimitProperties();
  private HeartbeatProperties heartbeat = new HeartbeatProperties();

  public BackpressureProperties getBackpressure() {
    return backpressure;
  }

  public void setBackpressure(BackpressureProperties backpressure) {
    this.backpressure = backpressure;
  }

  public RateLimitProperties getRateLimit() {
    return rateLimit;
  }

  public void setRateLimit(RateLimitProperties rateLimit) {
    this.rateLimit = rateLimit;
  }

  public HeartbeatProperties getHeartbeat() {
    return heartbeat;
  }

  public void setHeartbeat(HeartbeatProperties heartbeat) {
    this.heartbeat = heartbeat;
  }

  public static final class BackpressureProperties {
    private boolean enabled = true;
    private BackpressurePathConfig defaultConfig = new BackpressurePathConfig();
    private Map<String, BackpressurePathConfig> paths = new HashMap<>();

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public BackpressurePathConfig getDefaultConfig() {
      return defaultConfig;
    }

    public void setDefaultConfig(BackpressurePathConfig defaultConfig) {
      this.defaultConfig = defaultConfig;
    }

    public Map<String, BackpressurePathConfig> getPaths() {
      return paths;
    }

    public void setPaths(Map<String, BackpressurePathConfig> paths) {
      this.paths = paths;
    }
  }

  public static final class BackpressurePathConfig {
    // TODO: Extract to constants
    private Backpressure.BackpressureStrategy strategy = Backpressure.BackpressureStrategy.BUFFER;
    private int bufferSize = 256;

    public Backpressure.BackpressureStrategy getStrategy() {
      return strategy;
    }

    public void setStrategy(Backpressure.BackpressureStrategy strategy) {
      this.strategy = strategy;
    }

    public int getBufferSize() {
      return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
      this.bufferSize = bufferSize;
    }
  }

  public static final class RateLimitProperties {
    private boolean enabled = true;
    private RateLimitPathConfig defaultConfig = new RateLimitPathConfig();
    private Map<String, RateLimitPathConfig> paths = new HashMap<>();

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public RateLimitPathConfig getDefaultConfig() {
      return defaultConfig;
    }

    public void setDefaultConfig(RateLimitPathConfig defaultConfig) {
      this.defaultConfig = defaultConfig;
    }

    public Map<String, RateLimitPathConfig> getPaths() {
      return paths;
    }

    public void setPaths(Map<String, RateLimitPathConfig> paths) {
      this.paths = paths;
    }
  }

  public static final class RateLimitPathConfig {
    // TODO: Extract to constants
    private int limitForPeriod = 10;
    private long limitRefreshPeriod = 1L;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private long timeout = 25L;
    private RateLimit.RateLimitScope scope = RateLimit.RateLimitScope.SESSION;

    public int getLimitForPeriod() {
      return limitForPeriod;
    }

    public void setLimitForPeriod(int limitForPeriod) {
      this.limitForPeriod = limitForPeriod;
    }

    public long getLimitRefreshPeriod() {
      return limitRefreshPeriod;
    }

    public void setLimitRefreshPeriod(long limitRefreshPeriod) {
      this.limitRefreshPeriod = limitRefreshPeriod;
    }

    public TimeUnit getTimeUnit() {
      return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
      this.timeUnit = timeUnit;
    }

    public long getTimeout() {
      return timeout;
    }

    public void setTimeout(long timeout) {
      this.timeout = timeout;
    }

    public RateLimit.RateLimitScope getScope() {
      return scope;
    }

    public void setScope(RateLimit.RateLimitScope scope) {
      this.scope = scope;
    }
  }

  public static class HeartbeatProperties {
    private boolean enabled = true;
    private HeartbeatPathConfig defaultConfig = new HeartbeatPathConfig();
    private Map<String, HeartbeatPathConfig> paths = new HashMap<>();

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public HeartbeatPathConfig getDefaultConfig() {
      return defaultConfig;
    }

    public void setDefaultConfig(HeartbeatPathConfig defaultConfig) {
      this.defaultConfig = defaultConfig;
    }

    public Map<String, HeartbeatPathConfig> getPaths() {
      return paths;
    }

    public void setPaths(Map<String, HeartbeatPathConfig> paths) {
      this.paths = paths;
    }
  }

  public static final class HeartbeatPathConfig {
    // TODO: Extract to constants
    private long interval = 30L;
    private long timeout = 60L;

    public long getInterval() {
      return interval;
    }

    public void setInterval(long interval) {
      this.interval = interval;
    }

    public long getTimeout() {
      return timeout;
    }

    public void setTimeout(long timeout) {
      this.timeout = timeout;
    }
  }
}
