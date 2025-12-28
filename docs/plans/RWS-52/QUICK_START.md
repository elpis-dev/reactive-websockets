# Quick Start Guide: RWS-52 Implementation

**Issue:** [RWS-52 - Add Missing Tests](https://elpisdev.youtrack.cloud/issue/RWS-52)  
**Estimated Effort:** 19 hours (~2.5 days)  
**Target Coverage:** 90%+  

---

## 🎯 Quick Overview

Add comprehensive functional tests for:
1. **ReactiveWebSocketTemplate** (7% → 90%)
2. **WebSocketSessionRegistry** (40% → 90%)
3. **WebSocketHandlerFunction** (expand coverage)

---

## 🚀 Getting Started

### 1. Create Feature Branch
```bash
git checkout -b feature/RWS-52-add-missing-tests
```

### 2. Review Full Plan
📄 Read: `docs/plans/RWS-52/plan.md`

### 3. Set Up Test Structure
```bash
cd functional-tests/src/test/java/io/github/elpis/reactive/websockets/impl

mkdir -p template
mkdir -p session
mkdir -p handler
```

---

## 📋 Implementation Checklist

### Phase 1: Setup (2 hours)
- [ ] Create test package structure
- [ ] Add multi-client helpers to BaseWebSocketTest
- [ ] Create base test resource patterns

### Phase 2: ReactiveWebSocketTemplate (6 hours)
- [ ] Create ReactiveWebSocketTemplateResource.java
- [ ] Test sendBroadcast methods (single + Publisher)
- [ ] Test sendToSession methods (single + Publisher)
- [ ] Test sendToSessions method
- [ ] Test sendError and broadcastError methods
- [ ] Verify 90%+ coverage

### Phase 3: WebSocketSessionRegistry (4 hours)
- [ ] Create SessionRegistryResource.java
- [ ] Test register/unregister methods
- [ ] Test session retrieval methods
- [ ] Test session counting methods
- [ ] Test cleanup and shutdown methods
- [ ] Verify 90%+ coverage

### Phase 4: WebSocketHandlerFunction (5 hours)
- [ ] Create HandlerFunctionResource.java
- [ ] Expand query parameter tests
- [ ] Expand path variable tests
- [ ] Expand header tests
- [ ] Expand body tests
- [ ] Add exception handling tests

### Phase 5: Verification (2 hours)
- [ ] Run full test suite
- [ ] Check Codacy coverage
- [ ] Code review and cleanup
- [ ] Create summary document

---

## 🔑 Key Patterns

### Multi-Client Test Pattern
```java
@Test
void testBroadcast_MultipleClients() throws Exception {
    // Create sinks for 3 clients
    Sinks.One<String> sink1 = Sinks.one();
    Sinks.One<String> sink2 = Sinks.one();
    Sinks.One<String> sink3 = Sinks.one();
    
    String path = "/template/test";
    
    // Connect client 1
    withClient(path, session ->
        session.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(sink1::tryEmitValue)
            .then()
    ).subscribe();
    
    // Connect client 2 & 3 similarly...
    
    // Wait for connections to establish
    Thread.sleep(100);
    
    // Use template to broadcast
    template.sendBroadcast(path, "Hello All").block();
    
    // Verify all received
    StepVerifier.create(sink1.asMono())
        .expectNext("Hello All")
        .verify(Duration.ofSeconds(5));
        
    // Verify sink2 and sink3 similarly...
}
```

### Test Resource Pattern
```java
@Configuration
public class ReactiveWebSocketTemplateResource {
    
    @Bean
    public WebSocketHandlerFunction templateTestHandlers(
        ReactiveWebSocketTemplate template) {
        
        return route()
            .handle("/template/test/receiver", (request, sink) -> {
                // Simple echo receiver
                return request.receiveSession()
                    .flatMap(session -> sink.asFlux());
            })
            .handle("/template/test/sender", (request, sink) -> {
                // Uses template to send
                String sessionId = request.getSessionId();
                template.sendToSession("/template/test/receiver", 
                    sessionId, "Targeted Message");
                return sink.asFlux();
            });
    }
}
```

### AAA Pattern Template
```java
@Test
void testMethodName_Scenario_ExpectedBehavior() throws Exception {
    // Arrange
    String path = "/test/path";
    String testData = "test message";
    Sinks.One<String> resultSink = Sinks.one();
    
    // Act
    withClient(path, session -> 
        session.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(resultSink::tryEmitValue)
            .then()
    ).subscribe();
    
    // ... perform action ...
    
    // Assert
    StepVerifier.create(resultSink.asMono())
        .expectNext(testData)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
}
```

---

## 📊 Coverage Targets

### ReactiveWebSocketTemplate
Target uncovered lines:
- Lines 72-91: Broadcast methods
- Lines 99-137: Single session methods
- Lines 149-160: Multiple sessions methods
- Lines 174-214: Error methods

### WebSocketSessionRegistry
Target uncovered lines:
- Lines 92-106: Counting methods
- Lines 123-156: Maintenance methods

---

## 🧪 Running Tests

### Run Specific Test Class
```bash
mvn test -Dtest=ReactiveWebSocketTemplateTest -pl functional-tests
```

### Run All New Tests
```bash
mvn test -pl functional-tests
```

### Check Coverage Locally
```bash
mvn clean test jacoco:report
# View: report-aggregate/target/site/jacoco-aggregate/index.html
```

---

## ✅ Quality Checklist

Before committing:
- [ ] All tests pass (run 3 times minimum)
- [ ] No flaky tests
- [ ] AAA pattern followed
- [ ] StepVerifier used for reactive assertions
- [ ] Proper cleanup in doFinally() blocks
- [ ] Descriptive test names
- [ ] Comments for complex scenarios
- [ ] Javadoc for test classes
- [ ] Google Java Format applied
- [ ] No compiler warnings

---

## 📝 Commit Messages

Format:
```
RWS-52: <Brief description>

<Detailed description>

- Specific change 1
- Specific change 2
```

Example:
```
RWS-52: Add ReactiveWebSocketTemplate broadcast tests

Implemented comprehensive tests for broadcast functionality including:

- sendBroadcast with single payload
- sendBroadcast with Publisher payload
- Multi-client broadcast verification
- Empty path and no-session scenarios

Coverage increased from 7% to 95% for broadcast methods.
```

---

## 🆘 Troubleshooting

### Flaky Tests
**Symptom:** Tests pass sometimes, fail others  
**Solution:** 
- Increase timeouts
- Add synchronization points
- Use proper Sinks for communication
- Add small delays for connection establishment

### Resource Leaks
**Symptom:** Tests slow down over time  
**Solution:**
- Always use doFinally() for cleanup
- Properly close WebSocket sessions
- Clear sinks after use

### Coverage Not Increasing
**Symptom:** Coverage stays low after adding tests  
**Solution:**
- Check Codacy for specific uncovered lines
- Verify tests actually execute the code paths
- Add breakpoints to verify code is reached
- Check for early returns or exceptions

---

## 📚 Reference Documents

1. **Full Implementation Plan:** `plan.md` (this folder)
2. **Planning Summary:** `PLANNING_SUMMARY.md` (this folder)
3. **Testing Guide:** `../../.github/instructions/testing.instructions.md`
4. **Coding Style:** `../../.github/instructions/coding-style.instructions.md`
5. **YouTrack Issue:** https://elpisdev.youtrack.cloud/issue/RWS-52
6. **Codacy PR:** https://app.codacy.com/gh/elpis-dev/reactive-websockets/pull-requests/64

---

## 🎓 Learning Resources

### Reactor Testing
- [StepVerifier Guide](https://projectreactor.io/docs/core/release/reference/#testing)
- [Testing Reactive Streams](https://www.baeldung.com/reactive-streams-step-verifier-test-publisher)

### WebSocket Testing
- [Spring WebFlux Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#webtestclient)
- Existing tests in `impl/data/` for patterns

---

## 💡 Tips

1. **Start with simplest tests first** - Get familiar with patterns
2. **Test one scenario at a time** - Easier to debug
3. **Use existing tests as templates** - Copy patterns from HeaderSocketTest, etc.
4. **Monitor coverage continuously** - Check after each test class
5. **Don't over-complicate** - Simple, focused tests are best
6. **Use descriptive variable names** - Makes tests self-documenting
7. **Add comments for "why"** - Not "what" (code shows what)
8. **Keep tests fast** - Aim for < 5 seconds per test

---

## 🔄 Daily Workflow

### Start of Day
1. Pull latest changes
2. Review plan for the day's phase
3. Set up test infrastructure if needed
4. Review existing similar tests

### During Implementation
1. Implement tests following plan
2. Run tests frequently
3. Commit small, logical chunks
4. Monitor coverage changes
5. Document any deviations from plan

### End of Day
1. Run full test suite
2. Commit all working code
3. Push to remote
4. Update checklist in this document
5. Note any blockers or questions

---

## ✨ Success Criteria

You're done when:
- ✅ ReactiveWebSocketTemplate coverage ≥ 90%
- ✅ WebSocketSessionRegistry coverage ≥ 90%
- ✅ All WebSocketHandlerFunction scenarios tested
- ✅ All tests pass consistently (10+ runs)
- ✅ Test suite completes in < 30 seconds
- ✅ Code review completed
- ✅ PR merged to main

---

**Ready to start?** Begin with Phase 1 setup! 🚀

**Questions?** Review the full plan.md or ask your team.

**Got stuck?** Check the troubleshooting section above.

---

**Last Updated:** December 28, 2025  
**Status:** Ready for Implementation

