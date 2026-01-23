# RWS-52: Add Missing Tests - Planning Documentation

**Issue:** [RWS-52 - Add Missing Tests](https://elpisdev.youtrack.cloud/issue/RWS-52)  
**Status:** 🔄 Ready for Implementation  
**Priority:** 🔴 High  
**Created:** December 28, 2025  
**Estimated Effort:** 19 hours (~2.5 days)

---

## 📚 Document Index

This directory contains comprehensive planning documentation for implementing missing functional tests for critical components of the reactive-websockets framework.

### 📄 Primary Documents

#### 1. [plan.md](./plan.md) - **MAIN IMPLEMENTATION PLAN**
**Use this as your primary reference during implementation.**

Complete detailed implementation plan including:
- Executive summary and goals
- Requirements (functional and non-functional)
- Technical approach and architecture
- Phase-by-phase implementation breakdown
- Testing strategy
- Risk assessment and mitigation
- Timeline and milestones
- Acceptance criteria
- Architecture diagrams

**When to use:** Always - this is your master plan.

---

#### 2. [QUICK_START.md](./QUICK_START.md) - **START HERE**
**Read this first if you want to get started quickly.**

Quick reference guide containing:
- 🎯 Quick overview
- 🚀 Getting started steps
- 📋 Implementation checklist
- 🔑 Key code patterns
- 📊 Coverage targets
- 🧪 Running tests
- 💡 Tips and tricks

**When to use:** Before starting implementation, when you need quick reference.

---

#### 3. [CHECKLIST.md](./CHECKLIST.md) - **TRACK YOUR PROGRESS**
**Use this to track implementation progress.**

Comprehensive checklist including:
- Phase-by-phase task tracking
- Test-by-test completion tracking
- Coverage metrics tracking
- Daily progress log
- Git and PR checklist
- Final metrics and sign-off

**When to use:** Daily during implementation to track progress.

---

### 📊 Supporting Documents

#### 4. [PLANNING_SUMMARY.md](./PLANNING_SUMMARY.md)
**Summary of planning decisions and key findings.**

Contains:
- What was done during planning
- Key findings from Codacy analysis
- Plan highlights
- Success metrics
- Risk assessment
- Implementation timeline
- Next steps

**When to use:** For understanding the planning process, reviewing decisions.

---

#### 5. [COVERAGE_ANALYSIS.md](./COVERAGE_ANALYSIS.md)
**Detailed coverage analysis from Codacy PR #64.**

Contains:
- Component-by-component coverage breakdown
- Uncovered line ranges with code snippets
- Priority classification
- Expected impact of RWS-52
- Coverage monitoring visualizations
- Detailed recommendations

**When to use:** When you need to understand specific coverage gaps, when writing tests for uncovered lines.

---

## 🎯 Quick Navigation

### I want to...

**...get started immediately**
→ Read [QUICK_START.md](./QUICK_START.md)

**...understand the full plan**
→ Read [plan.md](./plan.md)

**...track my progress**
→ Use [CHECKLIST.md](./CHECKLIST.md)

**...understand coverage gaps**
→ Read [COVERAGE_ANALYSIS.md](./COVERAGE_ANALYSIS.md)

**...understand planning decisions**
→ Read [PLANNING_SUMMARY.md](./PLANNING_SUMMARY.md)

**...see code examples**
→ Check [QUICK_START.md](./QUICK_START.md) "Key Patterns" section

**...understand the architecture**
→ Check [plan.md](./plan.md) "Technical Approach" section

**...see the timeline**
→ Check [plan.md](./plan.md) "Timeline" section or [PLANNING_SUMMARY.md](./PLANNING_SUMMARY.md)

---

## 📋 Issue Summary

### Objective
Add comprehensive functional tests for three critical components:

1. **ReactiveWebSocketTemplate** (7.14% → 90% coverage)
   - Broadcasting methods
   - Single session methods
   - Multiple sessions methods
   - Error handling methods

2. **WebSocketSessionRegistry** (40.43% → 90% coverage)
   - Registration/unregistration
   - Session retrieval
   - Session counting
   - Maintenance methods

3. **WebSocketHandlerFunction** (expand coverage)
   - Query parameter extraction
   - Path variable extraction
   - Header extraction
   - Body extraction
   - Exception handling

### Why This Matters
- PR #64 introduced -17.15% coverage regression
- Only 57.3% diff coverage (208/363 lines)
- Critical infrastructure components untested
- Risk of bugs in production

### Success Criteria
- ✅ ReactiveWebSocketTemplate coverage ≥ 90%
- ✅ WebSocketSessionRegistry coverage ≥ 90%
- ✅ Comprehensive WebSocketHandlerFunction tests
- ✅ All tests pass consistently
- ✅ No flaky tests
- ✅ Test suite completes in < 30 seconds

---

## 🚀 Implementation Phases

### Phase 1: Setup (2 hours)
- Create test package structure
- Add multi-client helpers
- Create base test resources

### Phase 2: ReactiveWebSocketTemplate Tests (6 hours)
- Test broadcasting methods
- Test single session methods
- Test multiple sessions methods
- Test error methods
- **Target:** 90%+ coverage

### Phase 3: WebSocketSessionRegistry Tests (4 hours)
- Test registration/retrieval
- Test session counting
- Test maintenance methods
- **Target:** 90%+ coverage

### Phase 4: WebSocketHandlerFunction Tests (5 hours)
- Expand query parameter tests
- Expand path variable tests
- Expand header tests
- Expand body tests
- Add exception handling tests

### Phase 5: Verification (2 hours)
- Run full test suite
- Verify coverage with Codacy
- Code review and cleanup

**Total Estimated Effort:** 19 hours

---

## 📊 Expected Impact

### Coverage Improvements
| Component | Before | After | Improvement |
|-----------|--------|-------|-------------|
| ReactiveWebSocketTemplate | 7.14% | 90%+ | +82.86 pp |
| WebSocketSessionRegistry | 40.43% | 90%+ | +49.57 pp |
| Overall Project | Current | +5-8% | Significant |

### Quality Improvements
- ✅ Eliminate coverage regression (-17% → +10%)
- ✅ Critical components fully tested
- ✅ Error scenarios validated
- ✅ Concurrent access verified
- ✅ Resource cleanup validated

---

## 🔑 Key Patterns

### Multi-Client Testing
Tests use multiple concurrent WebSocket clients to simulate real-world usage:

```java
@Test
void testBroadcast_MultipleClients() {
    // Create 3 clients
    // Connect all clients to same path
    // Broadcast via template
    // Verify all received message
}
```

### Test Resources
Each test suite has accompanying resource configuration:

```java
@Configuration
public class ReactiveWebSocketTemplateResource {
    @Bean
    public WebSocketHandlerFunction handlers() {
        return route()
            .handle("/test/path", handler);
    }
}
```

### AAA Pattern
All tests follow Arrange-Act-Assert pattern:

```java
@Test
void testMethod_Scenario_Expected() {
    // Arrange: Setup
    // Act: Execute
    // Assert: Verify
}
```

---

## 📚 Related Documentation

### Project Documentation
- [Testing Guide](../../../.github/instructions/testing.instructions.md)
- [Coding Style Guide](../../../.github/instructions/coding-style.instructions.md)
- [Feature Development Workflow](../../../.github/instructions/feature-development.instructions.md)

### External References
- [YouTrack Issue RWS-52](https://elpisdev.youtrack.cloud/issue/RWS-52)
- [Codacy PR #64](https://app.codacy.com/gh/elpis-dev/reactive-websockets/pull-requests/64)
- [Spring WebFlux Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#webtestclient)
- [Reactor Testing Guide](https://projectreactor.io/docs/core/release/reference/#testing)

---

## 🔄 Document Versions

| Document | Version | Last Updated |
|----------|---------|--------------|
| plan.md | 1.0 | December 28, 2025 |
| QUICK_START.md | 1.0 | December 28, 2025 |
| CHECKLIST.md | 1.0 | December 28, 2025 |
| PLANNING_SUMMARY.md | 1.0 | December 28, 2025 |
| COVERAGE_ANALYSIS.md | 1.0 | December 28, 2025 |
| README.md (this file) | 1.0 | December 28, 2025 |

---

## ✅ Planning Status

### Completed ✅
- [x] YouTrack issue analysis
- [x] Codacy coverage analysis
- [x] Implementation plan creation
- [x] Planning summary creation
- [x] Quick start guide creation
- [x] Coverage analysis document
- [x] Implementation checklist creation
- [x] Documentation index (this file)

### Ready for Implementation 🚀
- [ ] Create feature branch
- [ ] Begin Phase 1: Setup
- [ ] Implement tests
- [ ] Verify coverage
- [ ] Create summary

---

## 🆘 Getting Help

### Questions About...

**Implementation approach**
→ See [plan.md](./plan.md) "Technical Approach" section

**Specific uncovered lines**
→ See [COVERAGE_ANALYSIS.md](./COVERAGE_ANALYSIS.md)

**Code patterns**
→ See [QUICK_START.md](./QUICK_START.md) "Key Patterns" section

**Progress tracking**
→ Use [CHECKLIST.md](./CHECKLIST.md)

**Planning decisions**
→ See [PLANNING_SUMMARY.md](./PLANNING_SUMMARY.md) "Key Decisions Made" section

**Troubleshooting**
→ See [QUICK_START.md](./QUICK_START.md) "Troubleshooting" section

### Contact
- Create comment on [RWS-52 YouTrack issue](https://elpisdev.youtrack.cloud/issue/RWS-52)
- Discuss with team
- Review existing test patterns in `functional-tests/src/test/java/.../impl/data/`

---

## 🎓 Learning Resources

### Reactive Testing
- [StepVerifier Guide](https://projectreactor.io/docs/core/release/reference/#testing)
- [Testing Reactive Streams](https://www.baeldung.com/reactive-streams-step-verifier-test-publisher)

### WebSocket Testing
- Existing tests: `functional-tests/src/test/java/.../impl/data/`
- [Spring WebFlux WebSocket Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-websocket)

### Best Practices
- Project [Testing Guide](../../../.github/instructions/testing.instructions.md)
- Project [Coding Style](../../../.github/instructions/coding-style.instructions.md)

---

## 💡 Tips for Success

1. **Read QUICK_START.md first** - Get oriented quickly
2. **Reference plan.md regularly** - Your master plan
3. **Use CHECKLIST.md daily** - Track progress
4. **Check COVERAGE_ANALYSIS.md** - Understand gaps
5. **Follow existing patterns** - Learn from HeaderSocketTest, BodySocketTest, etc.
6. **Test incrementally** - Small commits, frequent verification
7. **Monitor coverage continuously** - Check after each phase
8. **Keep tests simple** - Focused, clear, maintainable
9. **Use descriptive names** - Self-documenting tests
10. **Document complex scenarios** - Help future maintainers

---

## 📦 Deliverables

### Code Deliverables
- ReactiveWebSocketTemplateTest.java + Resource
- WebSocketSessionRegistryTest.java + Resource
- HandlerFunction*Test.java files + Resources
- Updated BaseWebSocketTest.java

### Documentation Deliverables
- ✅ Implementation plan (this directory)
- ⏳ Test documentation (Javadoc)
- ⏳ Implementation summary (post-completion)
- ⏳ Coverage screenshots (post-completion)

---

## 🎉 Ready to Start?

1. ✅ Read [QUICK_START.md](./QUICK_START.md)
2. ✅ Review [plan.md](./plan.md) 
3. ✅ Open [CHECKLIST.md](./CHECKLIST.md)
4. 🚀 Create feature branch
5. 🚀 Begin Phase 1 implementation

**Let's achieve 90% coverage and eliminate that -17% regression!**

---

**README Version:** 1.0  
**Created:** December 28, 2025  
**Status:** ✅ Planning Complete - Ready for Implementation  
**Issue:** [RWS-52](https://elpisdev.youtrack.cloud/issue/RWS-52)

