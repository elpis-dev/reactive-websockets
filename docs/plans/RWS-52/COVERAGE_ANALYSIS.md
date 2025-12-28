# Coverage Analysis: RWS-52

**Source:** [Codacy PR #64](https://app.codacy.com/gh/elpis-dev/reactive-websockets/pull-requests/64)  
**Analysis Date:** December 28, 2025  
**Analyzer:** GitHub Copilot (elpis-planning mode)

---

## Executive Summary

### Overall PR #64 Metrics
- **Delta Coverage:** -17.15% ⚠️ (REGRESSION)
- **Diff Coverage:** 57.3% (208/363 lines covered)
- **Coverable Lines in Diff:** 363 lines
- **Covered Lines in Diff:** 208 lines
- **Uncovered Lines in Diff:** 155 lines ❌
- **New Issues:** 0 ✅
- **Fixed Issues:** 0
- **Delta Clones:** +10 (duplication increased)
- **Up to Standards:** ❌ NO

### Critical Gap Analysis
This PR introduced significant new code but **only 57% is covered by tests**, resulting in a **-17% coverage regression**. Three components need immediate attention:

1. **ReactiveWebSocketTemplate:** 7.14% (52 lines uncovered) 🔴 CRITICAL
2. **WebSocketSessionRegistry:** 40.43% (27 lines uncovered) 🟠 HIGH
3. **BaseWebSocketHandler:** 30.65% (43 lines uncovered) 🟠 HIGH

---

## Component-by-Component Analysis

### 🔴 CRITICAL: ReactiveWebSocketTemplate.java

**Current Coverage:** 7.14%  
**Lines:** 4 covered / 56 coverable = **52 lines uncovered**  
**Priority:** CRITICAL (Primary target for RWS-52)

#### Uncovered Line Ranges

**Lines 72-91: Broadcast Methods (20 lines)**
```java
// ❌ 0% coverage
public Mono<Void> sendBroadcast(String path, Object payload) {
    return Mono.fromRunnable(() -> {
        Collection<SessionStreams> sessions = registry.getAllSessions(path);
        for (SessionStreams streams : sessions) {
            Sinks.EmitResult result = streams.outboundSink().tryEmitNext(payload);
            if (result.isFailure()) {
                log.warn("Failed to broadcast to session {} on path {}: {}",
                    streams.metadata().getSessionId(), path, result);
            }
        }
        log.debug("Broadcast completed to {} sessions on path {}", 
            sessions.size(), path);
    });
}

// ❌ 0% coverage
public Mono<Void> sendBroadcast(String path, Publisher<?> messages) {
    return Flux.from(messages).flatMap(msg -> sendBroadcast(path, msg)).then();
}
```

**Lines 99-137: Single Session Methods (39 lines)**
```java
// ❌ 0% coverage
public Mono<Void> sendToSession(String path, String sessionId, Object payload) {
    return Mono.fromRunnable(() -> {
        SessionStreams streams = registry.getSession(path, sessionId)
            .orElseThrow(() -> new SessionNotFoundException(
                String.format("Session %s not found on path %s", sessionId, path)));
        
        Sinks.EmitResult result = streams.outboundSink().tryEmitNext(payload);
        if (result.isFailure()) {
            log.warn("Failed to send message to session {}: {}", sessionId, result);
        }
    });
}

// ❌ 0% coverage
public Mono<Void> sendToSession(String path, String sessionId, 
                                 Publisher<?> messages) {
    return Flux.from(messages)
        .flatMap(msg -> sendToSession(path, sessionId, msg))
        .then();
}
```

**Lines 149-160: Multiple Sessions Methods (12 lines)**
```java
// ❌ 0% coverage
public Mono<Void> sendToSessions(String path, Set<String> sessionIds, 
                                  Object payload) {
    return Flux.fromIterable(sessionIds)
        .flatMap(sessionId ->
            sendToSession(path, sessionId, payload)
                .onErrorResume(SessionNotFoundException.class, e -> {
                    log.debug("Skipping non-existent session: {}", sessionId);
                    return Mono.empty();
                }))
        .then();
}
```

**Lines 174-214: Error Methods (41 lines)**
```java
// ❌ 0% coverage
public Mono<Void> sendError(String path, String sessionId, Object errorPayload) {
    return Mono.fromRunnable(() -> {
        SessionStreams streams = registry.getSession(path, sessionId)
            .orElseThrow(() -> new SessionNotFoundException(
                String.format("Session %s not found on path %s", sessionId, path)));
        
        Sinks.EmitResult result = streams.outboundSink()
            .tryEmitError(new ErrorResponseException(errorPayload));
        if (result.isFailure()) {
            log.warn("Failed to send error to session {}: {}", sessionId, result);
        }
    });
}

// ❌ 0% coverage
public Mono<Void> broadcastError(String path, Object errorPayload) {
    return Mono.fromRunnable(() -> {
        Collection<SessionStreams> sessions = registry.getAllSessions(path);
        for (SessionStreams streams : sessions) {
            Sinks.EmitResult result = streams.outboundSink()
                .tryEmitError(new ErrorResponseException(errorPayload));
            if (result.isFailure()) {
                log.warn("Failed to broadcast error to session {} on path {}: {}",
                    streams.metadata().getSessionId(), path, result);
            }
        }
    });
}
```

#### Coverage Impact
**Before RWS-52:** 7.14% (4/56 lines)  
**After RWS-52 Target:** 90%+ (50+/56 lines)  
**Expected Improvement:** +82.86 percentage points

---

### 🟠 HIGH PRIORITY: WebSocketSessionRegistry.java

**Current Coverage:** 40.43%  
**Lines:** 18 covered / 45 coverable = **27 lines uncovered**  
**Priority:** HIGH (Secondary target for RWS-52)

#### Covered Sections ✅
- Lines 26-46: Constructor, basic registration (covered)
- Lines 57-70: Basic retrieval methods (covered)
- Lines 82: Basic session queries (covered)

#### Uncovered Line Ranges

**Lines 92-106: Counting Methods (15 lines)**
```java
// Lines 92-93: ❌ NOT covered
public long getTotalSessionCount() {
    return totalSessions.get();
}

// Lines 99: ❌ NOT covered
public long getSessionCount(String path) {
    return Optional.ofNullable(pathRegistry.get(path))
        .map(PathSessions::getSessionCount)
        .orElse(0L);
}

// Lines 104-106: ❌ NOT covered
public Collection<String> getAllPaths() {
    return pathRegistry.keySet();
}
```

**Lines 123-156: Maintenance Methods (34 lines, 12 uncovered)**
```java
// Lines 123-145: Partially covered
public void cleanupOrphanedSessions() {
    log.debug("Running orphaned session cleanup check");
    int cleaned = 0;
    
    // Lines 129-143: ❌ NOT covered
    for (String path : getAllPaths()) {
        Collection<SessionStreams> sessions = getAllSessions(path);
        for (SessionStreams streams : sessions) {
            if (!streams.metadata().isOpen()) {
                log.warn("Found orphaned session (isOpen=false), cleaning up: {} on path {}",
                    streams.metadata().getSessionId(), path);
                unregisterSession(path, streams.metadata().getSessionId());
                cleaned++;
            }
        }
    }
    
    if (cleaned > 0) {
        log.info("Cleaned up {} orphaned sessions", cleaned);
    }
}

// Lines 149-153: ❌ NOT covered
public void shutdown() {
    log.info("Shutting down WebSocketSessionRegistry with {} sessions", 
        totalSessions.get());
    pathRegistry.values().forEach(PathSessions::shutdown);
    pathRegistry.clear();
    totalSessions.set(0);
}
```

#### Coverage Impact
**Before RWS-52:** 40.43% (18/45 lines)  
**After RWS-52 Target:** 90%+ (40+/45 lines)  
**Expected Improvement:** +49.57 percentage points

---

### 🟠 HIGH PRIORITY: BaseWebSocketHandler.java

**Current Coverage:** 30.65% (19/62 lines in diff)  
**Diff Coverage:** 30.65%  
**Priority:** HIGH (Not primary target, but significant gaps)

**Note:** This component is NOT a primary target for RWS-52 but has significant uncovered code that should be addressed in a future issue.

#### Major Uncovered Sections
- Lines 248-302: Error handling methods (54 lines, mostly uncovered)
- Lines 114-116: Initialization (uncovered)
- Lines 253-302: Stream composition (mostly uncovered)

This will require separate functional tests focused on error scenarios and edge cases.

---

### 🟡 MEDIUM: AdaptiveWebSocketHandler.java

**Current Coverage:** 83.81% (88/105 lines)  
**Diff Coverage:** 83.81%  
**Priority:** MEDIUM

Good coverage overall. Gaps are primarily in edge cases:
- Lines 143, 165, 189: Specific conditional branches
- Lines 228: Logging paths
- Lines 269-270, 306, 311, 316, 321, 326, 331, 336, 341, 346, 351, 356: Error handling paths

These can be addressed with targeted edge case tests in a future issue.

---

### 🟡 MEDIUM: PathSessions.java

**Current Coverage:** 76% (19/25 lines)  
**Diff Coverage:** 76%  
**Priority:** MEDIUM

#### Uncovered Lines
- Lines 51, 55, 63-66: Shutdown and cleanup methods (6 lines)

These are internal helper methods that will be tested indirectly through WebSocketSessionRegistry tests.

---

### 🟢 LOW PRIORITY: Well-Covered Components

#### WebSocketHandlerFunction.java
**Coverage:** 100% (2/2 lines) ✅  
**Status:** Excellent, but needs functional tests for parameter extraction

#### WebSocketHandlerFunctions.java
**Coverage:** 89.66% (26/29 lines)  
**Gaps:** Lines 60, 140-141 (edge cases)

#### RateLimiterService.java
**Coverage:** 87.5% (14/16 lines in diff)  
**Gaps:** Lines 85-86 (error handling)

#### SocketHandshakeService.java
**Coverage:** 100% (2/2 lines in diff) ✅

#### ErrorResponseException.java
**Coverage:** 100% (8/8 lines in diff) ✅

#### ReactiveWebSocketTemplateAutoConfiguration.java
**Coverage:** 100% (2/2 lines in diff) ✅

#### WebSocketEventManagerFactory.java
**Coverage:** 100% (1/1 line in diff) ✅

---

## Detailed Coverage Breakdown by File

### Priority 1: CRITICAL (Must Fix in RWS-52)

| File | Coverage | Covered | Coverable | Gap | Status |
|------|----------|---------|-----------|-----|--------|
| ReactiveWebSocketTemplate.java | 7.14% | 4 | 56 | 52 | 🔴 CRITICAL |

### Priority 2: HIGH (Must Fix in RWS-52)

| File | Coverage | Covered | Coverable | Gap | Status |
|------|----------|---------|-----------|-----|--------|
| WebSocketSessionRegistry.java | 40.43% | 18 | 45 | 27 | 🟠 HIGH |
| BaseWebSocketHandler.java | 30.65% | 19 | 62 | 43 | 🟠 HIGH* |

\* Not primary RWS-52 target, but significant gaps

### Priority 3: MEDIUM (Future Issues)

| File | Coverage | Covered | Coverable | Gap | Status |
|------|----------|---------|-----------|-----|--------|
| AdaptiveWebSocketHandler.java | 83.81% | 88 | 105 | 17 | 🟡 MEDIUM |
| PathSessions.java | 76% | 19 | 25 | 6 | 🟡 MEDIUM |
| WebSocketHandlerFunctions.java | 89.66% | 26 | 29 | 3 | 🟡 MEDIUM |
| RateLimiterService.java | 87.5% | 14 | 16 | 2 | 🟡 MEDIUM |

### Priority 4: LOW (Good Coverage)

| File | Coverage | Covered | Coverable | Gap | Status |
|------|----------|---------|-----------|-----|--------|
| WebSocketHandlerFunction.java | 100% | 2 | 2 | 0 | 🟢 GOOD |
| SocketHandshakeService.java | 100% | 2 | 2 | 0 | 🟢 GOOD |
| ErrorResponseException.java | 100% | 8 | 8 | 0 | 🟢 GOOD |
| ReactiveWebSocketTemplateAutoConfiguration.java | 100% | 2 | 2 | 0 | 🟢 GOOD |
| WebSocketEventManagerFactory.java | 100% | 1 | 1 | 0 | 🟢 GOOD |
| WebSocketRegistryMaintenanceConfig.java | 50% | 5 | 10 | 5 | 🟢 OK |
| ClosedConnectionHandlerConfiguration.java | N/A | 0 | 0 | 0 | 🟢 N/A |

---

## Test Coverage Recommendations

### Immediate Action (RWS-52)

#### 1. ReactiveWebSocketTemplate Tests (CRITICAL)
**Priority:** 🔴 P0  
**Estimated Effort:** 6 hours  
**Target Coverage:** 90%+

**Required Test Scenarios:**
- ✅ Broadcast to all sessions (single payload)
- ✅ Broadcast to all sessions (Publisher payload)
- ✅ Send to specific session (single payload)
- ✅ Send to specific session (Publisher payload)
- ✅ Send to multiple sessions
- ✅ Send error to session
- ✅ Broadcast error to all sessions
- ✅ Handle SessionNotFoundException
- ✅ Handle empty session sets
- ✅ Handle non-existent paths

**Test Approach:**
- Multiple concurrent WebSocket clients (3+)
- Real WebSocket connections (no mocks)
- Test all public methods
- Test all error paths
- Verify concurrent access safety

#### 2. WebSocketSessionRegistry Tests (HIGH)
**Priority:** 🟠 P1  
**Estimated Effort:** 4 hours  
**Target Coverage:** 90%+

**Required Test Scenarios:**
- ✅ Register/unregister sessions
- ✅ Get session by ID
- ✅ Get all sessions for path
- ✅ Get all paths
- ✅ Count total sessions
- ✅ Count sessions per path
- ✅ Cleanup orphaned sessions
- ✅ Shutdown registry
- ✅ Concurrent modifications
- ✅ Path cleanup when empty

**Test Approach:**
- Real session registration via WebSocket connections
- Test counting accuracy
- Test cleanup behavior
- Test shutdown process
- Verify log messages

#### 3. WebSocketHandlerFunction Tests (MEDIUM)
**Priority:** 🟡 P2  
**Estimated Effort:** 5 hours  
**Target Coverage:** Comprehensive functional tests

**Required Test Scenarios:**
- ✅ Query parameter extraction (all types)
- ✅ Path variable extraction (all types)
- ✅ Header extraction (all types)
- ✅ Body extraction (JSON, text)
- ✅ Optional parameters
- ✅ Default values
- ✅ Missing required parameters
- ✅ Invalid parameter types
- ✅ Multiple parameters combined
- ✅ Exception handling

**Test Approach:**
- Expand existing parameter tests
- Add comprehensive exception tests
- Test all parameter type combinations
- Test error messages

### Future Actions (Post-RWS-52)

#### 4. BaseWebSocketHandler Tests
**Priority:** 🟠 P1 (Future Issue)  
**Estimated Effort:** 6 hours  
**Target Coverage:** 80%+

Focus on error handling and edge cases currently at 30% coverage.

#### 5. AdaptiveWebSocketHandler Tests
**Priority:** 🟡 P2 (Future Issue)  
**Estimated Effort:** 2 hours  
**Target Coverage:** 90%+

Add tests for remaining edge cases and error paths.

---

## Expected Impact of RWS-52

### Coverage Improvements

| Component | Before | After | Improvement |
|-----------|--------|-------|-------------|
| ReactiveWebSocketTemplate | 7.14% | 90%+ | +82.86 pp |
| WebSocketSessionRegistry | 40.43% | 90%+ | +49.57 pp |
| Overall PR #64 Diff | 57.3% | 75%+ | +17.7 pp |
| Project Overall | Current | +5-8% | Significant |

### Quality Improvements
- ✅ Delta coverage regression eliminated (-17% → +10%)
- ✅ Critical components tested (ReactiveWebSocketTemplate)
- ✅ Session management verified (WebSocketSessionRegistry)
- ✅ Parameter extraction comprehensive (WebSocketHandlerFunction)
- ✅ Error scenarios validated
- ✅ Concurrent access tested
- ✅ Resource cleanup verified

---

## Coverage Monitoring

### Before RWS-52 Implementation
```
ReactiveWebSocketTemplate:     [████                            ] 7.14%
WebSocketSessionRegistry:      [████████████                    ] 40.43%
WebSocketHandlerFunction:      [████████████████████████████████] 100%*
BaseWebSocketHandler:          [█████████                       ] 30.65%
AdaptiveWebSocketHandler:      [██████████████████████████      ] 83.81%
```
\* But needs functional tests

### After RWS-52 Target
```
ReactiveWebSocketTemplate:     [█████████████████████████████   ] 90%+
WebSocketSessionRegistry:      [█████████████████████████████   ] 90%+
WebSocketHandlerFunction:      [████████████████████████████████] 100%
BaseWebSocketHandler:          [█████████                       ] 30.65%
AdaptiveWebSocketHandler:      [██████████████████████████      ] 83.81%
```

---

## Recommendations

### Immediate (RWS-52)
1. ✅ Implement ReactiveWebSocketTemplate tests (6 hours)
2. ✅ Implement WebSocketSessionRegistry tests (4 hours)
3. ✅ Expand WebSocketHandlerFunction tests (5 hours)
4. ✅ Verify 90% coverage target met
5. ✅ Monitor Codacy for coverage improvement

### Short-term (Next Sprint)
1. Create issue for BaseWebSocketHandler tests (30% → 80%)
2. Create issue for AdaptiveWebSocketHandler edge cases (83% → 90%)
3. Review and address duplication (+10 clones)

### Long-term (Future)
1. Establish coverage gates in CI/CD (minimum 80%)
2. Add coverage monitoring dashboard
3. Regular coverage reviews in sprint planning
4. Coverage regression prevention

---

## Appendix: Line-by-Line Coverage Details

### ReactiveWebSocketTemplate.java - Uncovered Lines
```
Lines 72-91:   sendBroadcast methods (20 lines)
Lines 99-137:  sendToSession methods (39 lines)
Lines 149-160: sendToSessions method (12 lines)
Lines 174-214: error methods (41 lines)
TOTAL: 112 lines in file, 52 uncovered in PR diff
```

### WebSocketSessionRegistry.java - Uncovered Lines
```
Lines 92-93:   getTotalSessionCount (2 lines)
Lines 99:      getSessionCount (1 line)
Lines 104-106: getAllPaths (3 lines)
Lines 129-143: cleanupOrphanedSessions loop (15 lines)
Lines 149-153: shutdown (5 lines)
TOTAL: 156 lines in file, 27 uncovered in PR diff
```

---

**Analysis Complete**  
**Next Step:** Begin RWS-52 implementation following the plan.md

**Coverage Target:** Achieve 90%+ coverage for ReactiveWebSocketTemplate and WebSocketSessionRegistry

**Estimated Time to Target:** 15-19 hours of focused test implementation

---

**Document Version:** 1.0  
**Created:** December 28, 2025  
**Data Source:** Codacy PR #64  
**Analyzer:** GitHub Copilot (elpis-planning mode)

