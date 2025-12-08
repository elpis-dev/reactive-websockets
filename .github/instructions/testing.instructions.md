# Testing Guide for Reactive WebSockets

This document provides comprehensive guidelines for writing, executing, and validating tests in the **Reactive WebSockets** project. Testing is a critical part of development and all contributions must include appropriate test coverage.

---

## Table of Contents
1. [Testing Framework & Tools](#testing-framework--tools)
2. [Test Types & Structure](#test-types--structure)
3. [Writing Unit Tests](#writing-unit-tests)
4. [Writing Integration Tests](#writing-integration-tests)
5. [Running Tests](#running-tests)
6. [Test Naming Conventions](#test-naming-conventions)
7. [Best Practices](#best-practices)
8. [Code Coverage](#code-coverage)
9. [Troubleshooting](#troubleshooting)
10. [References](#references)

---

## Testing Framework & Tools

### Core Dependencies

The project uses the following testing frameworks and libraries:

| Tool | Purpose | Version |
|------|---------|---------|
| **JUnit 5** | Test framework | Via Spring Boot |
| **Project Reactor Test** | Reactive stream testing | Via Spring Boot |
| **AssertJ** | Fluent assertions | Via Spring Boot |
| **StepVerifier** | Reactor stream verification | Part of reactor-test |
| **Mockito** | Mocking framework | Via Spring Boot |
| **LogCaptor** | Log verification | 2.9.3 |
| **Spring Boot Test** | Integration test support | 3.3.1 |
| **Testcontainers** | Container-based testing | Optional |

### Maven Dependencies

All testing dependencies are managed in the parent `pom.xml`. Key test-scoped dependencies:

```xml
<!-- JUnit 5 Platform and Engine -->
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>

<!-- Spring Boot Test Starter -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>

<!-- Project Reactor Test -->
<dependency>
  <groupId>io.projectreactor</groupId>
  <artifactId>reactor-test</artifactId>
  <scope>test</scope>
</dependency>

<!-- LogCaptor for Log Testing -->
<dependency>
  <groupId>nl.altindag</groupId>
  <artifactId>logcaptor</artifactId>
  <version>2.9.3</version>
  <scope>test</scope>
</dependency>
```

---

## Test Types & Structure

The project uses two primary test categories:

### 1. **Unit Tests**
**Purpose:** Test individual classes/methods in isolation.

**Location:** 
```
functional-tests/src/test/java/org/elpis/reactive/websockets/impl/<feature>/
```

**Characteristics:**
- Fast execution (< 100ms per test)
- No Spring context required
- Mock external dependencies
- Test a single responsibility
- No real network or I/O operations

**When to write:**
- Testing utility classes (e.g., `JsonMapperTest`)
- Testing business logic
- Testing error handling paths
- Testing edge cases

### 2. **Integration Tests (Functional Tests)**
**Purpose:** Test feature behavior with real Spring context and WebSocket connections.

**Location:**
```
functional-tests/src/test/java/org/elpis/reactive/websockets/impl/<feature>/
```

**Characteristics:**
- Slower execution (1-10 seconds per test)
- Full Spring Boot context
- Real WebSocket client/server connections
- Test end-to-end flows
- Use test configuration and resources

**When to write:**
- Testing WebSocket handlers (`CloseTest`, `PingTest`)
- Testing data binding (`BodySocketTest`, `HeaderSocketTest`)
- Testing security flows (`AnonymousAuthenticationTest`)
- Testing routing and event handling

---

## Writing Unit Tests

### Test Structure (AAA Pattern)

All tests should follow the **Arrange-Act-Assert** (AAA) pattern:

```java
@Test
void descriptiveTestName() {
  // Arrange - Setup test data and mocks
  final String input = "test";
  final Object expectedResult = new Object();

  // Act - Execute the code under test
  final Object actualResult = classUnderTest.method(input);

  // Assert - Verify the result
  assertThat(actualResult).isEqualTo(expectedResult);
}
```

### Example: Unit Test for Utility Class

```java
package org.elpis.reactive.websockets.impl.util;

import org.elpis.reactive.websockets.mapper.JsonMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JsonMapper}.
 *
 * @author Test Author
 * @since 1.0.0
 */
class JsonMapperTest {

  private final JsonMapper jsonMapper = new JsonMapper();

  @Test
  void stringInputReturnedAsIs() {
    // Arrange
    final String input = "already a string";

    // Act
    final String result = JsonMapper.applyWithFallback(input);

    // Assert
    assertThat(result).isEqualTo(input);
  }

  @Test
  void objectConvertedToJsonString() {
    // Arrange
    final TestObject obj = new TestObject("name", 42);

    // Act
    final String result = JsonMapper.applyWithFallback(obj);

    // Assert
    assertThat(result).contains("name").contains("42");
  }

  @Test
  void invalidObjectThrowsException() {
    // Arrange
    final Object invalid = new Object();

    // Act & Assert
    assertThatThrownBy(() -> JsonMapper.applyWithFallback(invalid))
        .isInstanceOf(RuntimeJsonMappingException.class);
  }

  @Test
  void applyWithDefaultReturnsDefaultOnException() {
    // Arrange
    final Object invalid = new Object();
    final String defaultValue = "default";

    // Act
    final String result = JsonMapper.applyWithDefault(invalid, defaultValue);

    // Assert
    assertThat(result).isEqualTo(defaultValue);
  }

  // Test data class
  static class TestObject {
    private final String name;
    private final int age;

    TestObject(String name, int age) {
      this.name = name;
      this.age = age;
    }
  }
}
```

### Using Assertions

Use **AssertJ** for fluent, readable assertions:

```java
// String assertions
assertThat(result)
  .isNotNull()
  .hasSize(5)
  .contains("expected")
  .startsWith("prefix");

// Numeric assertions
assertThat(count)
  .isPositive()
  .isGreaterThan(0)
  .isLessThanOrEqualTo(100);

// Collection assertions
assertThat(list)
  .hasSize(3)
  .contains("a", "b", "c")
  .doesNotContain("d");

// Exception assertions
assertThatThrownBy(() -> method())
  .isInstanceOf(CustomException.class)
  .hasMessageContaining("error text");
```

---

## Writing Integration Tests

### Test Configuration

Integration tests extend `BaseWebSocketTest` which provides:
- WebSocket client utilities
- Random data generators
- Security configuration
- Spring Boot test context

### Base Test Class Structure

```java
/**
 * Base class for WebSocket integration tests.
 *
 * <p>Provides common utilities for WebSocket client connections, random data generation,
 * and test environment setup.
 *
 * @author Test Author
 * @since 1.0.0
 */
public abstract class BaseWebSocketTest {
  public static final String DEFAULT_TEST_PROFILE = "test";

  protected static final Duration DEFAULT_GENERIC_TEST_FALLBACK = Duration.ofSeconds(10L);
  protected static final Duration DEFAULT_FAST_TEST_FALLBACK = Duration.ofSeconds(5L);

  @LocalServerPort
  private Integer port;

  /**
   * Executes a WebSocket client connection with the given path and handler.
   *
   * @param path the WebSocket path
   * @param webSocketHandler the handler for the session
   * @return a Mono that completes when the connection is closed
   * @throws URISyntaxException if the path is invalid
   */
  public Mono<Void> withClient(final String path,
                               final Function<WebSocketSession, Mono<Void>> webSocketHandler)
      throws URISyntaxException { ... }

  // Random data generators
  public String randomTextString(final int length) { ... }
  public byte getRandomByte() { ... }
  public int getRandomInteger() { ... }
  // ... more generators
}
```

### Example: Integration Test for WebSocket Handler

```java
package org.elpis.reactive.websockets.impl.connection;

import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.resource.connection.CloseResource;
import org.elpis.reactive.websockets.context.resource.connection.CloseRoutingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

/**
 * Integration tests for WebSocket close behavior.
 *
 * Tests verify that WebSocket close signals are properly handled and
 * communicated to connected clients.
 *
 * @author Test Author
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class
)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({
    BaseWebSocketTest.PermitAllSecurityConfiguration.class,
    CloseRoutingConfiguration.class,
    CloseResource.class
})
class CloseTest extends BaseWebSocketTest {

  @Test
  void normalCloseFromServer() throws Exception {
    // Arrange
    final String path = "/close/normal";
    final Sinks.One<Integer> sink = Sinks.one();

    // Act
    this.withClient(path, session -> session.closeStatus()
        .doOnNext(closeStatus -> sink.tryEmitValue(closeStatus.getCode()))
        .then())
        .subscribe();

    // Assert
    StepVerifier.create(sink.asMono())
        .expectNext(CloseStatus.NORMAL.getCode())
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void goingAwayCloseFromServer() throws Exception {
    // Arrange
    final String path = "/close/goingAway";
    final Sinks.One<Integer> sink = Sinks.one();

    // Act
    this.withClient(path, session -> session.closeStatus()
        .doOnNext(closeStatus -> sink.tryEmitValue(closeStatus.getCode()))
        .then())
        .subscribe();

    // Assert
    StepVerifier.create(sink.asMono())
        .expectNext(CloseStatus.GOING_AWAY.getCode())
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }
}
```

### Testing Reactive Streams with StepVerifier

**StepVerifier** is used to test Project Reactor streams:

```java
// Test for successful completion
StepVerifier.create(monoOrFlux)
    .expectNext(expectedValue)
    .expectComplete()
    .verify(Duration.ofSeconds(10));

// Test for multiple values
StepVerifier.create(flux)
    .expectNext("value1", "value2", "value3")
    .expectComplete()
    .verify();

// Test for error
StepVerifier.create(monoWithError)
    .expectError(CustomException.class)
    .verify();

// Test with assertions
StepVerifier.create(flux)
    .assertNext(item -> assertThat(item).isNotNull())
    .assertNext(item -> assertThat(item).hasSize(5))
    .expectComplete()
    .verify();
```

### Testing Data Binding

```java
package org.elpis.reactive.websockets.impl.data;

import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.resource.data.MessageBodySocketResource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

/**
 * Integration tests for WebSocket message body binding.
 *
 * @author Test Author
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class
)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, MessageBodySocketResource.class})
class BodySocketTest extends BaseWebSocketTest {

  @Test
  void receiveAndProcessTextMessage() throws Exception {
    // Arrange
    final String path = "/body/post";
    final Sinks.One<String> responseCapture = Sinks.one();
    final String testMessage = "Test message payload";

    // Act
    this.withClient(path, session -> {
      // Send a message and capture the response
      return session.send(Flux.just(session.textMessage(testMessage)))
          .then(session.receive()
              .doOnNext(msg -> responseCapture.tryEmitValue(msg.getPayloadAsText()))
              .then());
    })
        .subscribe();

    // Assert
    StepVerifier.create(responseCapture.asMono())
        .expectNext(testMessage)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void receiveBinaryMessage() throws Exception {
    // Arrange
    final String path = "/body/post/binary";
    final Sinks.One<String> sink = Sinks.one();

    // Act
    this.withClient(path, session -> session.receive()
        .filter(webSocketMessage -> webSocketMessage.getType() == WebSocketMessage.Type.BINARY)
        .doOnNext(webSocketMessage -> sink.tryEmitValue(webSocketMessage.getPayloadAsText()))
        .then())
        .subscribe();

    // Assert
    StepVerifier.create(sink.asMono())
        .expectNext("Binary")
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }
}
```

### Testing Security

```java
package org.elpis.reactive.websockets.impl.security;

import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

/**
 * Integration tests for anonymous authentication flows.
 *
 * Verifies that WebSocket endpoints properly handle unauthenticated connections
 * with anonymous principals.
 *
 * @author Test Author
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class
)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class})
class AnonymousAuthenticationTest extends BaseWebSocketTest {

  @Test
  void anonymousPrincipalCreatedForUnauthenticatedRequest() throws Exception {
    // Arrange
    final String path = "/secure/anonymous";
    final Sinks.One<String> principalCapture = Sinks.one();

    // Act
    this.withClient(path, session -> session.receive()
        .doOnNext(msg -> principalCapture.tryEmitValue(msg.getPayloadAsText()))
        .then())
        .subscribe();

    // Assert
    StepVerifier.create(principalCapture.asMono())
        .expectNext("anonymous")
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }
}
```

### Testing with Log Verification

Use **LogCaptor** to verify logging behavior:

```java
import nl.altindag.log.LogCaptor;
import static org.assertj.core.api.Assertions.assertThat;

@Test
void logsCapturedForProcessing() throws Exception {
  // Arrange
  final LogCaptor logCaptor = LogCaptor.forClass(ProcessorClass.class);
  final String testInput = "test data";

  // Act
  classUnderTest.process(testInput);

  // Assert
  assertThat(logCaptor.getInfoLogs())
      .contains("Processing started")
      .contains("Test data received");

  assertThat(logCaptor.getWarnLogs())
      .isEmpty();

  assertThat(logCaptor.getErrorLogs())
      .isEmpty();
}
```

---

## Running Tests

### Maven Commands

#### Run All Tests
```bash
mvn clean test
```

#### Run Specific Test Class
```bash
mvn test -Dtest=CloseTest
```

#### Run Specific Test Method
```bash
mvn test -Dtest=CloseTest#normalCloseFromServer
```

#### Run Tests in Specific Module
```bash
mvn test -pl functional-tests
```

#### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

#### Skip Tests During Build
```bash
mvn clean package -DskipTests
```

#### Run Tests with Verbose Output
```bash
mvn test -X
```

### IDE Execution

#### IntelliJ IDEA
1. **Run single test:** Right-click on test class/method → **Run**
2. **Run with coverage:** Right-click on test class/method → **Run with Coverage**
3. **Run all tests in file:** Right-click on test file → **Run**
4. **Run all tests in package:** Right-click on package → **Run Tests in 'package'**

#### Other IDEs
Similar context-menu options are available in Eclipse, VS Code, NetBeans, etc.

### Continuous Integration

Tests run automatically on:
- Push to branches
- Pull requests
- Scheduled nightly builds

Verify test configuration in `.github/workflows/`.

---

## Test Naming Conventions

### Test Class Naming

**Pattern:** `<Feature>Test.java`

**Examples:**
```
CloseTest.java                          // WebSocket close behavior
PingTest.java                           // Ping/Pong frames
BodySocketTest.java                     // Message body binding
HeaderSocketTest.java                   // HTTP header binding
PathVariableSocketTest.java             // Path variable binding
QueryParameterSocketTest.java           // Query parameter binding
SessionTest.java                        // Session management
RoutingTest.java                        // Endpoint routing
AnonymousAuthenticationTest.java        // Anonymous auth flow
SecurityChainTest.java                  // Security filter chain
JsonMapperTest.java                     // JSON utility testing
```

### Test Method Naming

**Pattern:** `<action><expectedOutcome>` or `<givenCondition><whenAction><thenExpectation>`

**Examples:**
```java
// Simple pattern
void textMessageReceivedSuccessfully() { ... }
void binaryMessageReceivedSuccessfully() { ... }
void invalidMessageThrowsException() { ... }
void sessionClosedOnTimeout() { ... }

// Given-When-Then pattern
void givenAuthenticatedUser_whenConnecting_thenAllowConnection() { ... }
void givenInvalidPath_whenConnecting_thenDenyConnection() { ... }
void givenMultipleMessages_whenReceived_thenAllProcessed() { ... }
void givenServerError_whenHandling_thenLogAndContinue() { ... }
```

### Avoid

```
❌ test1(), test2()                     // Non-descriptive names
❌ testSomething()                      // Too vague
❌ test_with_underscores()              // Mixed styles
❌ normalCloseFromServerAndVerifyCode() // Too long, redundant
```

---

## Best Practices

### 1. **One Assertion Per Test (Single Responsibility)**
```java
// Good - Tests one specific behavior
@Test
void normalCloseReturnsCorrectCode() {
  // ... test code ...
  assertThat(code).isEqualTo(CloseStatus.NORMAL.getCode());
}

// Acceptable - Related assertions
@Test
void closeResponseIsValid() {
  // ... test code ...
  assertThat(closeStatus)
      .isNotNull()
      .hasCode(1000)
      .hasReason("Normal");
}
```

### 2. **Test Edge Cases and Error Conditions**
```java
class UserServiceTest {
  @Test
  void createUserWithValidData() { ... }

  @Test
  void createUserWithNullNameThrowsException() { ... }

  @Test
  void createUserWithEmptyEmailThrowsException() { ... }

  @Test
  void createUserWithDuplicateEmailReturnsError() { ... }
}
```

### 3. **Use Descriptive Test Data**
```java
// Good - Clear intent
final String validEmail = "user@example.com";
final String invalidEmail = "not-an-email";

// Avoid - Unclear purpose
final String email = "test@test.com";
final String s = "foo@bar";
```

### 4. **Mock External Dependencies**
```java
@Test
void userServiceCallsRepositoryOnCreate() {
  // Arrange
  final UserRepository mockRepository = mock(UserRepository.class);
  final UserService service = new UserService(mockRepository);
  final User newUser = new User("John");

  // Act
  service.create(newUser);

  // Assert
  verify(mockRepository).save(newUser);
}
```

### 5. **Don't Create Test Interdependencies**
```java
// Bad - Tests depend on each other
@Test
void testAcreateSomeData() { ... }
@Test
void testBreliesOnDataFromTestA() { ... }

// Good - Each test is independent
@Test
void testAshoulWorkOnItsOwn() { ... }
@Test
void testBshoulWorkOnItsOwn() { ... }
```

### 6. **Use Test Fixtures for Common Setup**
```java
class WebSocketHandlerTest {
  private WebSocketHandler handler;
  private MockSession session;

  @BeforeEach
  void setUp() {
    handler = new WebSocketHandler();
    session = new MockSession();
  }

  @AfterEach
  void tearDown() {
    session.close();
  }

  @Test
  void testOne() { ... }

  @Test
  void testTwo() { ... }
}
```

### 7. **Test Timeout Behavior**
```java
@Test
void connectionTimeoutAfterExpectedDuration() {
  StepVerifier.create(connectionFlow)
      .expectTimeout(Duration.ofSeconds(5))
      .verify();
}
```

### 8. **Verify Reactive Stream Behavior**
```java
@Test
void fluxEmitsThreeValuesInSequence() {
  final Flux<String> flux = Flux.just("a", "b", "c");

  StepVerifier.create(flux)
      .expectNext("a")
      .expectNext("b")
      .expectNext("c")
      .expectComplete()
      .verify();
}
```

### 9. **Document Test Intent with Comments**
```java
@Test
void verifyCloseStatusCodePropagatedToClient() {
  // The server sends a NORMAL close status (1000) to the client.
  // We verify that the client receives the correct status code
  // and can properly handle the connection closure.

  // Arrange
  final String path = "/close/normal";
  final Sinks.One<Integer> statusCodeCapture = Sinks.one();

  // Act & Assert
  // ... test implementation ...
}
```

### 10. **Use @ParameterizedTest for Multiple Scenarios**
```java
@ParameterizedTest
@ValueSource(strings = {"validEmail@test.com", "another@example.org"})
void validEmailsAreAccepted(String email) {
  assertThat(validator.isValid(email)).isTrue();
}

@ParameterizedTest
@CsvSource({
    "null,             false",
    "invalid,          false",
    "test@test.com,    true"
})
void variousEmailFormats(String email, boolean expected) {
  assertThat(validator.isValid(email)).isEqualTo(expected);
}
```

---

## Code Coverage

### JaCoCo Configuration

The project uses **JaCoCo** (Java Code Coverage) to measure test coverage:

```bash
# Generate coverage report
mvn clean test jacoco:report

# Report location
target/site/jacoco/index.html
```

### Coverage Goals

**Target coverage:** 80%+ for critical paths
- Controllers/Handlers: 85%+
- Business logic: 80%+
- Configuration: 70%+
- Exception handling: 90%+

### Viewing Coverage in IDE

**IntelliJ IDEA:**
1. Right-click on test class → **Run 'ClassName' with Coverage**
2. View coverage highlighting in code editor
3. Check coverage statistics in left margin

### Improving Coverage

```java
// Identify untested code paths
public void method() {
  if (condition) {
    // Test this branch
    doSomething();
  } else {
    // Test this branch too
    doOtherThing();
  }
}

// Write tests for both branches
@Test
void conditionIsTruePath() { ... }

@Test
void conditionIsFalsePath() { ... }
```

---

## Troubleshooting

### Common Issues and Solutions

#### Issue: Tests Fail with TimeoutException

**Symptom:**
```
reactor.core.publisher.Exceptions$TimeoutException: Did not complete within timeout
```

**Solutions:**
1. Increase timeout duration:
   ```java
   .verify(DEFAULT_GENERIC_TEST_FALLBACK.plus(Duration.ofSeconds(5)))
   ```

2. Check if WebSocket server is running properly
3. Verify network/port availability
4. Check for blocking operations in async code

#### Issue: Port Already in Use

**Symptom:**
```
java.net.BindException: Address already in use
```

**Solutions:**
1. Kill process using the port:
   ```bash
   # macOS/Linux
   lsof -i :8080
   kill -9 <PID>
   ```

2. Use random port in test:
   ```java
   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
   ```

#### Issue: MockWebSocketSession Not Available

**Symptom:**
```
Cannot resolve symbol 'MockWebSocketSession'
```

**Solutions:**
1. Extend `BaseWebSocketTest` for proper setup
2. Use Spring's test utilities
3. Create mock manually using Mockito

#### Issue: Reactive Stream Not Completing

**Symptom:**
```
StepVerifier did not emit complete signal
```

**Solutions:**
1. Verify the stream actually completes:
   ```java
   .then()  // Completes the stream
   ```

2. Check for infinite streams:
   ```java
   Flux.interval(...).take(10)  // Add boundary
   ```

3. Verify subscription occurred:
   ```java
   .subscribe()  // Ensure subscription
   ```

#### Issue: Spring Context Fails to Load

**Symptom:**
```
org.springframework.context.ApplicationContextException: Failed to start bean
```

**Solutions:**
1. Check test profile is correct:
   ```java
   @ActiveProfiles({"test"})
   ```

2. Verify configuration class is imported:
   ```java
   @Import({TestConfiguration.class})
   ```

3. Check dependencies are available in test scope

#### Issue: Security Filter Blocks Test Connection

**Symptom:**
```
403 Forbidden or connection refused
```

**Solutions:**
1. Use PermitAllSecurityConfiguration in test:
   ```java
   @Import(BaseWebSocketTest.PermitAllSecurityConfiguration.class)
   ```

2. Add necessary authentication headers
3. Check security rules in test configuration

---

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.org/docs/)
- [Project Reactor Testing](https://projectreactor.io/docs/core/release/reference/#testing)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [JaCoCo Coverage](https://www.jacoco.org/jacoco/trunk/doc/index.html)
- [LogCaptor GitHub](https://github.com/Hakky54/log-captor)

---

**Test Coverage Enforcement:** All pull requests must maintain or improve code coverage. Tests with less than 80% coverage for critical paths may be rejected. This guide is a living document and may be updated as testing practices evolve.

