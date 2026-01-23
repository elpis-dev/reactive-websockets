package io.github.elpis.reactive.websockets.config.flowcontrol;

import static io.github.elpis.reactive.websockets.Constants.DEFAULT_BACKPRESSURE_BUFFER_CAPACITY;
import static io.github.elpis.reactive.websockets.Constants.DEFAULT_BACKPRESSURE_STRATEGY;
import static io.github.elpis.reactive.websockets.Constants.DEFAULT_HEARTBEAT_INTERVAL;
import static io.github.elpis.reactive.websockets.Constants.DEFAULT_HEARTBEAT_TIMEOUT;
import static io.github.elpis.reactive.websockets.Constants.DEFAULT_RATE_LIMIT_FOR_PERIOD;
import static io.github.elpis.reactive.websockets.Constants.DEFAULT_RATE_LIMIT_REFRESH_PERIOD;
import static io.github.elpis.reactive.websockets.Constants.DEFAULT_RATE_LIMIT_SCOPE;
import static io.github.elpis.reactive.websockets.Constants.DEFAULT_RATE_LIMIT_TIMEOUT;
import static io.github.elpis.reactive.websockets.Constants.DEFAULT_RATE_LIMIT_TIME_UNIT;

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
    private Backpressure.BackpressureStrategy strategy = DEFAULT_BACKPRESSURE_STRATEGY;
    private int bufferSize = DEFAULT_BACKPRESSURE_BUFFER_CAPACITY;

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
    private int limitForPeriod = DEFAULT_RATE_LIMIT_FOR_PERIOD;
    private long limitRefreshPeriod = DEFAULT_RATE_LIMIT_REFRESH_PERIOD;
    private TimeUnit timeUnit = DEFAULT_RATE_LIMIT_TIME_UNIT;
    private long timeout = DEFAULT_RATE_LIMIT_TIMEOUT;
    private RateLimit.RateLimitScope scope = DEFAULT_RATE_LIMIT_SCOPE;

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
    private long interval = DEFAULT_HEARTBEAT_INTERVAL;
    private long timeout = DEFAULT_HEARTBEAT_TIMEOUT;

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
