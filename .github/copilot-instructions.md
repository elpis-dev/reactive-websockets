# GitHub Copilot Instructions for Reactive WebSockets

This document serves as the main instructions for the GitHub Copilot agent working on the **Reactive WebSockets** project. It summarizes key principles and references detailed custom instructions for specific domains.

---

## Table of Contents
1. [Overview](#overview)
2. [Core Principles](#core-principles)
3. [Quick Reference](#quick-reference)
4. [Custom Instructions](#custom-instructions)
5. [Project Structure](#project-structure)
6. [Key Technologies](#key-technologies)
7. [Getting Help](#getting-help)

---

## Overview

The **Reactive WebSockets** project is a Spring Boot-based framework for building reactive WebSocket applications. It uses:

- **Java 17** for modern language features
- **Spring Boot 3.3+** for application framework
- **Spring WebFlux** for reactive web
- **Project Reactor** for reactive streams
- **Maven** for build management

As a Copilot agent, your role is to:
- Help developers understand the codebase
- Generate code following project conventions
- Implement features according to specifications
- Write tests with appropriate coverage
- Maintain code quality and consistency

---

## Core Principles

### 1. **Project-First Approach**
- Always understand the project structure before making changes
- Follow existing patterns and conventions
- Reference relevant modules and packages
- Check if similar implementations exist before creating new code

### 2. **Quality & Testing**
- Every feature must include unit tests (minimum 80% coverage)
- Integration tests required for WebSocket functionality
- Tests must be placed in the `functional-tests` module
- Use descriptive test names and the AAA pattern (Arrange-Act-Assert)

### 3. **Multi-Module Architecture**
The project is organized into distinct modules with clear responsibilities:

| Module | Purpose | Key Packages |
|--------|---------|--------------|
| `reactive-websockets-model` | Interfaces, annotations, domain models | `event`, `web.annotation`, `exception` |
| `reactive-websockets-annotation-processor` | Compile-time code generation (APT) | `processor` |
| `reactive-websockets-starter` | Core implementation and auto-config | `config`, `event`, `handler`, `session`, `security` |
| `functional-tests` | Integration tests and examples | `impl.<feature>` |
| `report-aggregate` | Test coverage reporting | - |

### 4. **Code Style & Documentation**
- Follow Google Java Format standards
- All public classes require Javadoc with `@author` and `@since` tags
- Method-level Javadoc required for public methods
- Use meaningful variable names and avoid code duplication
- See **coding-style.instructions.md** for detailed guidelines

### 5. **Feature Development Workflow**
The standard workflow for feature implementation:

```
1. Retrieve YouTrack ticket (validate it has proper details)
   ↓
2. Create development plan (in plan/{ISSUE_ID}/plan.md)
   ↓
3. Create feature branch (feature/{ISSUE_ID}-short-name)
   ↓
4. Implement feature + tests (following code style guide)
   ↓
5. Create summary (in summary/{ISSUE_ID}/summary.md)
```

See **feature-development.instructions.md** for comprehensive workflow details.

---

## Quick Reference

### Common Tasks

#### Adding a New Class
1. Determine correct module and package (see `coding-style.instructions.md`)
2. Create class with proper Javadoc
3. Follow naming conventions (e.g., `EventManagerImpl`, `WebSocketConfiguration`)
4. Add unit/integration tests
5. Achieve minimum 80% code coverage

#### Creating a Feature
1. Retrieve ticket from YouTrack
2. Create development plan
3. Create feature branch
4. Implement following all guidelines
5. Write comprehensive tests
6. Create summary upon completion

#### Writing Tests
1. Use JUnit 5 and AssertJ for unit tests
2. Use `StepVerifier` for reactive stream testing
3. Use `@SpringBootTest` for integration tests
4. Follow AAA pattern (Arrange-Act-Assert)
5. See **testing.instructions.md** for detailed examples

#### Code Review Checklist
- [ ] Code follows Google Java Format
- [ ] All public classes/methods have Javadoc
- [ ] Test coverage ≥ 80%
- [ ] Classes placed in correct module/package
- [ ] No code duplication
- [ ] Uses reactive patterns appropriately
- [ ] Security considerations addressed
- [ ] Commit messages reference ticket ID

---

## Custom Instructions

This project maintains detailed custom instructions for specific domains. Always reference the appropriate guide:

### 1. **Coding Style Guide**
**File:** `coding-style.instructions.md`

**Covers:**
- Package structure and organization
- Module responsibilities
- Class naming conventions
- Contributing new classes step-by-step
- Javadoc standards
- Project-specific development notes

**When to use:**
- Creating new classes or components
- Organizing code in modules
- Writing Javadoc and documentation
- Understanding package structure

**Key sections:**
- Package Structure
- Contributing New Classes
- Javadoc and Documentation Standards
- Project-Specific Notes

---

### 2. **Feature Development Workflow**
**File:** `feature-development.instructions.md`

**Covers:**
- Complete feature development workflow
- YouTrack ticket retrieval and validation
- Development planning process
- Git branch creation and naming
- Execution guidelines
- Summary documentation

**When to use:**
- Starting work on a new feature
- Creating development plans
- Managing feature branches
- Planning implementation approach

**Key sections:**
- Workflow Overview
- Ticket Validation
- Development Planning
- Branch Naming & Creation
- Execution Guidelines
- Agent Instructions

---

### 3. **Testing Guide**
**File:** `testing.instructions.md`

**Covers:**
- Testing frameworks and tools
- Unit test writing patterns
- Integration test examples
- StepVerifier usage for reactive streams
- Test naming conventions
- Code coverage requirements
- Running tests

**When to use:**
- Writing unit or integration tests
- Testing reactive components
- Verifying test coverage
- Debugging test failures

**Key sections:**
- Testing Framework & Tools
- Test Types & Structure
- Writing Unit Tests
- Writing Integration Tests
- Running Tests
- Best Practices

---

## Project Structure

### Directory Organization

```
reactive-websockets (root)
├── .github/
│   ├── workflows/              # CI/CD pipelines
│   └── instructions/           # This folder - custom instructions
│       ├── copilot-instructions.md    # Main Copilot instructions (this file)
│       ├── coding-style.instructions.md
│       ├── feature-development.instructions.md
│       └── testing.instructions.md
├── docs/                       # Project documentation
├── reactive-websockets-model/  # Interfaces, annotations, models
├── reactive-websockets-annotation-processor/  # APT code generation
├── reactive-websockets-starter/  # Main implementation
├── functional-tests/           # Integration tests
├── report-aggregate/           # Coverage reports
├── pom.xml                     # Parent POM
└── README.md                   # Project README
```

### Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent Maven POM, dependency management |
| `.github/workflows/maven-publish.yml` | Maven Central publication CI/CD |
| `README.md` | Project overview and setup |
| `.gitignore` | Git ignore patterns |
| `.sdkmanrc` | SDK version management |

---

## Key Technologies

### Language & Framework
- **Java 17** - Modern JDK with records, sealed classes, pattern matching
- **Spring Boot 3.3+** - Application framework
- **Spring WebFlux** - Reactive web framework
- **Spring Security** - Authentication and authorization

### Reactive Programming
- **Project Reactor** - Reactive streams implementation
- **Mono** - Single-element async sequence
- **Flux** - Multi-element async sequence
- **Sinks** - Programmatic emission of signals

### Testing
- **JUnit 5** - Test framework
- **AssertJ** - Fluent assertions
- **Mockito** - Mocking framework
- **StepVerifier** - Reactor stream verification
- **LogCaptor** - Log capturing for testing

### Build & CI/CD
- **Maven 3.8+** - Build tool
- **GitHub Actions** - CI/CD pipelines
- **GPG** - Code signing for Maven Central

---

## Development Workflow Summary

### Before Starting
1. Read the relevant custom instruction file for your task
2. Understand the project's multi-module structure
3. Check existing code for similar patterns
4. Verify you have the right module and package

### During Development
1. Follow coding style guidelines
2. Write code incrementally with tests
3. Use descriptive commit messages with ticket IDs
4. Keep methods focused and readable
5. Document with Javadoc and comments

### After Development
1. Ensure test coverage ≥ 80%
2. Verify all public classes have Javadoc
3. Run full test suite
4. Create summary documentation
5. Prepare for code review

---

## Getting Help

### Understanding the Codebase
- **Project structure:** See `coding-style.instructions.md` → Package Structure
- **Module responsibilities:** See `coding-style.instructions.md` → Module Descriptions
- **Package organization:** See `coding-style.instructions.md` → Package Naming Convention

### Implementing Features
- **Workflow:** See `feature-development.instructions.md` → Workflow Overview
- **Planning:** See `feature-development.instructions.md` → Create Development Plan
- **Branching:** See `feature-development.instructions.md` → Create Development Branch

### Writing Tests
- **Frameworks & tools:** See `testing.instructions.md` → Testing Framework & Tools
- **Unit tests:** See `testing.instructions.md` → Writing Unit Tests
- **Integration tests:** See `testing.instructions.md` → Writing Integration Tests
- **Examples:** See `testing.instructions.md` → Integration test code examples

### Code Quality
- **Naming conventions:** See `coding-style.instructions.md` → Class Naming Conventions
- **Javadoc standards:** See `coding-style.instructions.md` → Javadoc and Documentation Standards
- **Code organization:** See `coding-style.instructions.md` → Contributing New Classes

---

## Quick Commands Reference

### Maven
```bash
# Clean build
mvn clean install

# Run tests
mvn test

# Build specific module
mvn clean install -pl reactive-websockets-starter -am

# Skip tests
mvn clean install -DskipTests

# Run with specific profile
mvn clean install -Prelease

# Code coverage report
mvn test jacoco:report
```

### Git
```bash
# Create feature branch
git checkout -b feature/TICKET-ID-description

# Commit with ticket reference
git commit -m "TICKET-ID: Brief description"

# View branch history
git log --oneline -10

# Push changes
git push -u origin feature/TICKET-ID-description
```

---

## Important Notes

### Code Formatting
- The project uses **Google Java Format**
- Format check is integrated into CI/CD pipeline
- IDE integration recommended for automatic formatting
- See `coding-style.instructions.md` for details

### Test Coverage
- Minimum required: **80%** code coverage
- Integration tests count towards coverage
- Coverage reports generated in `report-aggregate` module
- Review `testing.instructions.md` for testing best practices

### Reactive Programming
- Always prefer `Mono` and `Flux` over imperative code
- Use `StepVerifier` for testing reactive streams
- Be mindful of subscription and error handling
- See examples in `testing.instructions.md`

### Javadoc Requirements
- **Every public class** must have class-level Javadoc
- **Every public method** must have method-level Javadoc
- Include `@author` and `@since` tags
- Use `@param`, `@return`, `@throws` for methods
- Provide usage examples for complex classes

---

## Document Versions

This instructions document is part of a set of coordinated custom instructions:

| Document | Purpose | Last Updated |
|----------|---------|--------------|
| copilot-instructions.md | Main Copilot instructions | 2025-12-09 |
| coding-style.instructions.md | Code style and project structure | 2025-12-09 |
| feature-development.instructions.md | Feature workflow and planning | 2025-12-09 |
| testing.instructions.md | Testing frameworks and patterns | 2025-12-09 |

---

## Feedback & Improvements

If you find these instructions unclear or incomplete:
1. Note the specific section and issue
2. Create a ticket in YouTrack with improvements
3. Reference the document: `copilot-instructions.md`
4. Suggest concrete improvements

These instructions evolve as the project develops. Keep them updated!

---

**Last Updated:** December 9, 2025  
**Version:** 1.0.0-alpha  
**Maintained By:** Reactive WebSockets Development Team

