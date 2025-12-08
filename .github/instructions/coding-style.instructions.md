# Coding Style Guide

This repository is a multi-module Java project using Java 17, Spring Boot, Spring WebFlux, and related frameworks.

---

## Table of Contents
1. [Package Structure](#package-structure)
2. [Contributing New Classes](#contributing-new-classes)
3. [Project-Specific Notes](#project-specific-notes)
4. [References](#references)

### CI/CD Integration
The project uses Maven for building. Formatting checks should be integrated into the build pipeline. Consider using the `fmt-maven-plugin` or similar tools to enforce formatting during CI builds.

---

## Package Structure

The **Reactive WebSockets** project follows a well-organized multi-module Maven structure. Understanding where to place new code is essential for maintainability.

### Project Overview
```
reactive-websockets (parent pom)
├── reactive-websockets-model           (Data models, interfaces, annotations)
├── reactive-websockets-annotation-processor (APT for code generation)
├── reactive-websockets-starter         (Main implementation & Spring Boot auto-config)
├── functional-tests                    (Integration tests)
└── report-aggregate                    (Test coverage reporting)
```

### Module Descriptions

#### 1. **reactive-websockets-model**
**Base domain and contract definitions. Use this module for shared interfaces and data models.**

```
org.elpis.reactive.websockets
├── web/
│   └── annotation/                  # Socket-related annotations
│       ├── @SocketMapping            # Main socket endpoint annotation
│       ├── @SocketController         # Controller-level annotation
│       └── ...
├── event/
│   ├── model/                       # Event domain models (interfaces)
│   │   ├── WebSocketEvent           # Base event interface
│   │   └── ...
│   └── annotation/
├── security/
│   ├── principal/                   # Security principals
│   │   ├── Anonymous                # Anonymous principal
│   │   └── ...
│   └── filter/                      # Security filters (interfaces)
├── session/                         # Session domain models
├── config/                          # Configuration interfaces
├── exception/                       # Custom exceptions
└── util/                            # Utility classes
```

**When to add classes here:**
- Creating a new event type that will be used across modules
- Defining a new annotation for framework features
- Creating a shared exception
- Defining security-related domain models
- Adding common utility functions used by multiple modules

**Example class naming:**
```java
// In org.elpis.reactive.websockets.event.model
public interface WebSocketEvent<T> { ... }

// In org.elpis.reactive.websockets.web.annotation
@Target(ElementType.METHOD)
public @interface SocketMapping { ... }
```

---

#### 2. **reactive-websockets-annotation-processor**
**Compile-time annotation processing for code generation. Generates handler implementations.**

```
org.elpis.reactive.websockets
└── processor/
    ├── SocketHandlerProcessor       # Main APT processor
    ├── SocketEndpointProcessor      # Endpoint processor
    └── ...
```

**When to add classes here:**
- Adding new annotation processors
- Extending code generation logic
- Creating utility classes for APT (annotation processing tool)

**Rules:**
- Classes here run at compile-time
- Use `javax.annotation.processing` API
- Generate classes in `org.elpis.reactive.websockets.generated` package
- Be cautious with dependencies (avoid runtime dependencies)

**Example:**
```java
// In org.elpis.reactive.websockets.processor
@SupportedAnnotationTypes("org.elpis.reactive.websockets.web.annotation.SocketMapping")
public class SocketHandlerProcessor extends AbstractProcessor { ... }
```

---

#### 3. **reactive-websockets-starter** (Main Module)
**Core runtime implementation. This is where most development happens.**

```
org.elpis.reactive.websockets
├── EnableReactiveSockets.java              # Entry point annotation
├── EnableReactiveSocketSecurity.java       # Security entry point
├── config/
│   ├── WebSocketConfiguration.java         # Main configuration
│   ├── event/
│   │   ├── WebSocketEventConfiguration      # Event system setup
│   │   ├── EventManagerConfiguration        # Event manager setup
│   │   └── ClosedConnectionHandlerConfiguration
│   ├── handler/
│   │   ├── route/
│   │   │   └── WebSocketRouteConfiguration  # Route mapping setup
│   │   └── ...
│   ├── security/
│   │   └── WebSocketSecurityConfiguration   # Security setup
│   └── session/
│       └── WebSocketSessionConfiguration    # Session setup
├── event/
│   ├── EventSelectorProcessor.java          # Event routing
│   ├── manager/
│   │   ├── WebSocketEventManager.java       # Event publishing interface
│   │   ├── WebSocketEventManagerFactory.java
│   │   └── impl/
│   │       └── MulticastEventManager.java   # Multicast implementation
│   └── matcher/
│       ├── EventSelectorMatcher.java        # Event matching interface
│       └── impl/
│           └── ClosedSessionEventSelectorMatcher.java
├── handler/
│   ├── BaseWebSocketHandler.java            # Core websocket handler
│   ├── BroadcastWebSocketResourceHandler.java
│   ├── route/
│   │   ├── WebSocketHandlerFunction.java    # Function interface
│   │   ├── WebSocketHandlerFunctions.java   # Function utilities
│   │   └── WebSocketHandlerRouteResolver.java
│   └── ...
├── session/
│   ├── ReactiveWebSocketSession.java        # Session model
│   ├── WebSocketSessionRegistry.java        # Session storage
│   ├── WebSocketSessionContext.java         # Session context
│   └── ...
├── security/
│   ├── SocketHandshakeService.java          # Handshake security
│   ├── filter/
│   └── principal/
├── mapper/
│   └── JsonMapper.java                      # JSON utilities
└── exception/
    └── ...                                  # Runtime exceptions
```

**Package organization rules:**

- **`config/`** → Spring `@Configuration` classes
  - Organize by feature: `event/`, `handler/`, `security/`, `session/`
  - Separate configuration concerns
  - Use descriptive names: `WebSocketEventConfiguration`, `WebSocketSecurityConfiguration`

- **`event/`** → Event system implementation
  - **`event/manager/`** → Event publishing/listening
  - **`event/matcher/`** → Event selection/routing logic
  - **`event/impl/`** → Implementation classes of interfaces from model module

- **`handler/`** → WebSocket handlers
  - **`handler/route/`** → Route resolution and function mapping
  - Base handlers (`BaseWebSocketHandler`)
  - Specialized handlers (`BroadcastWebSocketResourceHandler`)

- **`session/`** → Session management
  - Session storage (`WebSocketSessionRegistry`)
  - Session context/metadata
  - Session-related operations

- **`security/`** → Security implementation
  - Authentication/authorization logic
  - Handshake security services
  - Security filters/interceptors

- **`mapper/`** → Data transformation utilities
  - JSON mapping helpers
  - Type conversion utilities

---

#### 4. **functional-tests**
**Integration tests using the framework. Real-world usage examples.**

```
org.elpis.reactive.websockets.impl         # Test implementations
├── connection/
│   ├── CloseTest.java
│   └── PingTest.java
├── data/
│   ├── BodySocketTest.java
│   ├── HeaderSocketTest.java
│   ├── PathVariableSocketTest.java
│   ├── QueryParameterSocketTest.java
│   └── SessionTest.java
├── event/
│   └── ClosedSessionEventSelectorMatcherTest.java
├── routing/
│   └── RoutingTest.java
├── security/
│   ├── AnonymousAuthenticationTest.java
│   ├── AnonymousFallbackAuthenticationTest.java
│   ├── AnonymousFallbackAuthenticationFilterTest.java
│   └── ExchangeMatcherTest.java
└── ...
```

**When to add test classes:**
- Testing new features end-to-end
- Adding integration scenarios
- Verifying security behavior
- Testing event handling

**Naming convention:** `<Feature>Test.java` (e.g., `WebSocketSecurityTest.java`)

---

### Package Naming Convention

The project uses reverse domain naming:
- **Base package:** `org.elpis.reactive.websockets`
- **Sub-packages:** Organized by functional domain

**Do NOT create:**
- Package names with underscores (`_`)
- Overly nested packages (max 6-7 levels)
- Single-class packages

**Example of correct naming:**
```
✓ org.elpis.reactive.websockets.config.security
✓ org.elpis.reactive.websockets.event.manager.impl
✓ org.elpis.reactive.websockets.handler.route

✗ org.elpis.reactive.websockets.config_security
✗ org.elpis.reactive.websockets.event_manager_impl
✗ org.elpis.reactive.websockets.handler.route.resolver.impl.default
```

---

## Contributing New Classes

### Step 1: Determine the Correct Module
| What are you creating? | Module | Package |
|---|---|---|
| Event interface/model | `reactive-websockets-model` | `org.elpis.reactive.websockets.event.model` |
| Annotation | `reactive-websockets-model` | `org.elpis.reactive.websockets.web.annotation` |
| Exception | `reactive-websockets-model` | `org.elpis.reactive.websockets.exception` |
| Annotation processor | `reactive-websockets-annotation-processor` | `org.elpis.reactive.websockets.processor` |
| Event manager implementation | `reactive-websockets-starter` | `org.elpis.reactive.websockets.event.manager.impl` |
| Handler implementation | `reactive-websockets-starter` | `org.elpis.reactive.websockets.handler` |
| Spring configuration | `reactive-websockets-starter` | `org.elpis.reactive.websockets.config.<feature>` |
| Utility class | `reactive-websockets-starter` | `org.elpis.reactive.websockets.<feature>` |
| Integration test | `functional-tests` | `org.elpis.reactive.websockets.impl.<feature>` |

### Step 2: Follow Class Naming Conventions

| Class Type | Naming Pattern | Example |
|---|---|---|
| Interface | `<Concept>` | `WebSocketEventManager`, `EventSelectorMatcher` |
| Implementation | `<Concept><Impl\|Type>` | `MulticastEventManager`, `ClosedSessionEventSelectorMatcher` |
| Configuration | `<Feature>Configuration` | `WebSocketEventConfiguration`, `WebSocketSecurityConfiguration` |
| Service/Manager | `<Concept>Service` or `<Concept>Manager` | `SocketHandshakeService`, `WebSocketEventManager` |
| Processor (APT) | `<Concept>Processor` | `SocketHandlerProcessor` |
| Test | `<Feature>Test` | `RoutingTest`, `AnonymousAuthenticationTest` |
| Handler | `<Type>WebSocketHandler` | `BaseWebSocketHandler`, `BroadcastWebSocketResourceHandler` |

### Step 3: Create the Class with Proper Structure

**Template for a new service/manager class:**
```java
package org.elpis.reactive.websockets.event.manager.impl;

import org.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;
import reactor.core.publisher.Sinks;

/**
 * Implementation of {@link WebSocketEventManager} using multicast semantics.
 *
 * @param <T> the event type
 * @author [Your Name]
 * @since 1.0.0
 */
public class MulticastEventManager<T extends WebSocketEvent<?>> implements WebSocketEventManager<T> {

  private final Sinks.Multicast<T> sink;

  public MulticastEventManager() {
    this.sink = Sinks.multicast().onBackpressureBuffer(MEDIUM_EVENT_QUEUE_SIZE);
  }

  @Override
  public Sinks.EmitResult fire(final T event) {
    return this.sink.tryEmitNext(event);
  }

  // Additional methods...
}
```

**Template for a configuration class:**
```java
package org.elpis.reactive.websockets.config.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for WebSocket events.
 *
 * @author [Your Name]
 * @since 1.0.0
 */
@Configuration
public class WebSocketEventConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public EventManagerConfiguration eventManagerConfiguration() {
    return new EventManagerConfiguration();
  }

  // Additional beans...
}
```

### Step 4: Add Comprehensive Javadoc

**Javadoc requirements:**
- Every public class must have a class-level Javadoc
- Every public method must have Javadoc
- Include `@author` and `@since` tags
- Use `@param`, `@return`, `@throws` tags for methods
- Provide usage examples for complex classes

**Example:**
```java
/**
 * Registry for managing active WebSocket sessions.
 *
 * <p>This registry stores all active {@link ReactiveWebSocketSession} instances and provides
 * methods to retrieve, save, and remove sessions. Thread-safe via {@link ConcurrentHashMap}.
 *
 * @author Alex Zharkov
 * @see ReactiveWebSocketSession
 * @since 1.0.0
 */
public final class WebSocketSessionRegistry {

  /**
   * Saves a WebSocket session to the registry.
   *
   * @param session the session to save, must not be null
   * @return the previous session with the same ID, or null if none existed
   * @throws IllegalArgumentException if session is null
   * @since 1.0.0
   */
  public ReactiveWebSocketSession save(final ReactiveWebSocketSession session) {
    // Implementation...
  }
}
```

### Step 5: Add Tests

For every public class in `reactive-websockets-starter`:
- Add a unit test in the same module
- Add an integration test in `functional-tests` if applicable

**Test class location:**
```
functional-tests/src/test/java/org/elpis/reactive/websockets/impl/<feature>/YourClassTest.java
```

---

## Javadoc and Documentation Standards

### Class-Level Javadoc
```java
/**
 * Brief description of the class.
 *
 * <p>Optional detailed explanation of functionality, behavior, and usage patterns.
 * Can span multiple paragraphs.
 *
 * <p>Example usage:
 * <pre>
 *   YourClass instance = new YourClass();
 *   instance.doSomething();
 * </pre>
 *
 * @author Author Name
 * @see RelatedClass
 * @since 1.0.0
 */
public class YourClass { ... }
```

### Method-Level Javadoc
```java
/**
 * Retrieves the session with the specified ID.
 *
 * @param sessionId the session identifier, must not be null or empty
 * @return an {@link Optional} containing the session if found, otherwise empty
 * @throws IllegalArgumentException if sessionId is null or empty
 * @see Optional
 * @since 1.0.0
 */
public Optional<ReactiveWebSocketSession> get(final String sessionId) { ... }
```

---

## Project-Specific Notes

### Spring Boot & WebFlux
- All configuration classes should be annotated with `@Configuration`
- Use `@ConditionalOnMissingBean` for optional beans
- Prefer constructor injection over field injection
- Use reactive types (`Mono`, `Flux`) from Project Reactor
- Follow Spring naming conventions for beans

### Annotation Processor & Model Modules
- Model classes should be immutable where possible
- Use `final` keyword for fields and classes that shouldn't be extended
- Annotation processors must handle errors gracefully

### Tests
- Use descriptive test method names: `testSomethingReturnsExpectedValue()`
- Test code should be as readable as production code
- Include both positive and negative test cases

### Dependency Management
- All dependencies are managed in the parent `pom.xml`
- Do not add dependencies to individual module POMs without consulting the team
- Java 17+ features are allowed (records, sealed classes, pattern matching)

### Code Quality
- Avoid code duplication; extract common logic
- Keep methods focused and small (ideal: <30 lines)
- Use meaningful variable names (avoid single-letter variables except for loops/streams)
- Prefer composition to inheritance
- Use functional programming paradigms where appropriate with Project Reactor

---

## References
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Project Reactor Documentation](https://projectreactor.io/)
- [Java 17 Features](https://openjdk.java.net/projects/jdk/17/)
- [Maven Multi-Module Projects](https://maven.apache.org/guides/mini/guide-multiple-modules.html)

---
