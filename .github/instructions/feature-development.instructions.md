# Feature Development Workflow Guide

This document outlines the comprehensive workflow for developing features in the **Reactive WebSockets** project using YouTrack tickets, development planning, and code organization.

---

## Table of Contents
1. [Workflow Overview](#workflow-overview)
2. [Step 1: Retrieve Ticket from YouTrack](#step-1-retrieve-ticket-from-youtrack)
3. [Step 2: Validate Ticket](#step-2-validate-ticket)
4. [Step 3: Create Development Plan](#step-3-create-development-plan)
5. [Step 4: Create Development Branch](#step-4-create-development-branch)
6. [Step 5: Execute Development](#step-5-execute-development)
7. [Step 6: Create/Update Summary](#step-6-createupdate-summary)
8. [Agent Instructions](#agent-instructions)
9. [File Structure](#file-structure)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Workflow Overview

The feature development workflow follows a structured process:

```
1. RETRIEVE YOUTRACK TICKET
   ↓
2. VALIDATE TICKET (Reject if empty)
   ↓
3. PLANNING PHASE (Create/Extend plan/{ISSUE_ID}/plan.md)
   ↓
4. CREATE GIT BRANCH (<type>/{ISSUE_ID}-short-description)
   ↓
5. EXECUTION PHASE (Implement feature)
   ↓
6. UPDATE SUMMARY (Create/Extend summary/{ISSUE_ID}/summary.md)
```

**Critical Principle:** Agent performs planning FIRST, then execution. Never create summary unless requested.

---

## Step 1: Retrieve Ticket from YouTrack

### Using the YouTrack API

Before starting any feature development, retrieve the ticket details from YouTrack.

**Command for Agents:**
```
Use mcp_youtrack-mcp_get_issue tool with issue ID (e.g., DEMO-123, PROJ-456)
```

**What to Retrieve:**
- Issue ID and key
- Summary/Title
- Description
- Type (Bug, Task, Feature, Improvement)
- Status (Open, In Progress, Done)
- Assignee
- Priority
- Due date
- Custom fields (if applicable)
- Comments/Discussion

### Example Issue Retrieval

```python
# Agent retrieves ticket
issue = mcp_youtrack_get_issue(issueId="DEMO-123")

# Extract information
issue_id = issue["id"]
summary = issue["summary"]
description = issue["description"]
issue_type = issue["customFields"]["Type"]
status = issue["customFields"]["State"]
```

---

## Step 2: Validate Ticket

### Validation Criteria

A ticket is **valid** if it meets these requirements:

✓ **Has Issue ID** - Format: `PROJECT-NUMBER` (e.g., `DEMO-123`)
✓ **Has Summary** - Non-empty, descriptive title
✓ **Has Description** - Detailed explanation of requirements
✓ **Has Type** - Bug, Task, Feature, or Improvement
✓ **Has Status** - Not in "Done" or "Closed" state

### Rejection Criteria

A ticket is **invalid** and must be rejected if:

✗ **Empty Summary** - No title provided
✗ **Empty Description** - No requirements specified
✗ **No Issue ID** - Cannot create tracking artifacts
✗ **Already Completed** - Status is "Done" or "Closed"
✗ **Missing Critical Information** - Cannot determine requirements

### Agent Rejection Response

If ticket validation fails, the agent should:

1. **Provide clear reason** - Why the ticket is invalid
2. **Request information** - What's missing or needs clarification
3. **Suggest next steps** - How to fix the issue
4. **Do not proceed** - Stop all processing until fixed

**Rejection Template:**
```
❌ TICKET VALIDATION FAILED

Issue: [ISSUE_ID]
Summary: [Summary if available]

❌ Reason: [Specific reason for rejection]

Required to proceed:
- [Missing item 1]
- [Missing item 2]
- [Missing item 3]

Please update the ticket with the required information and resubmit.
```

**Example Rejection:**
```
❌ TICKET VALIDATION FAILED

Issue: DEMO-999
Summary: (empty)

❌ Reason: Ticket has empty summary and description

Required to proceed:
- Provide a clear, descriptive title
- Add detailed requirements and acceptance criteria
- Specify the type of work (Bug, Task, Feature)

Please update the ticket in YouTrack and resubmit.
```

---

## Step 3: Create Development Plan

### Planning Phase Overview

The planning phase is **critical** and must be completed BEFORE any code is written.

**Planning Objectives:**
- Understand requirements thoroughly
- Identify affected modules
- Plan implementation approach
- Estimate effort
- Identify dependencies
- Outline testing strategy

### Plan File Structure

**Location:** `plan/{YOUTRACK_ISSUE_ID}/plan.md`

**Example:** `plan/DEMO-123/plan.md`

### Plan Template

```markdown
# Feature Development Plan: [ISSUE_ID]

## Ticket Information
- **Issue ID:** [DEMO-123]
- **Summary:** [Feature title]
- **Type:** [Bug/Task/Feature/Improvement]
- **Priority:** [High/Medium/Low]
- **Status:** [Open/In Progress]
- **Assignee:** [Developer name]

## Requirements Analysis

### Functional Requirements
1. [Requirement 1]
2. [Requirement 2]
3. [Requirement 3]

### Non-Functional Requirements
- [Performance requirement]
- [Security requirement]
- [Compatibility requirement]

### Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

## Impact Analysis

### Affected Modules
- `reactive-websockets-model` - [Changes needed]
- `reactive-websockets-starter` - [Changes needed]
- `reactive-websockets-annotation-processor` - [Changes needed]
- `functional-tests` - [Test changes needed]

### Affected Packages
- `org.elpis.reactive.websockets.event.*`
- `org.elpis.reactive.websockets.handler.*`
- `org.elpis.reactive.websockets.config.*`

### Dependencies
- [Internal dependency 1]
- [External dependency 1]
- [Blocked by ticket X]

## Implementation Plan

### Phase 1: Model & Interfaces (Estimated: [2-4 hours])
- [ ] Create/modify interfaces in `reactive-websockets-model`
- [ ] Define domain models
- [ ] Add/update annotations
- [ ] Update exception classes

### Phase 2: Implementation (Estimated: [4-8 hours])
- [ ] Implement core logic in `reactive-websockets-starter`
- [ ] Create handler/manager classes
- [ ] Add configuration beans
- [ ] Integrate with existing components

### Phase 3: Testing (Estimated: [2-4 hours])
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Achieve 80%+ coverage
- [ ] Manual testing

### Phase 4: Documentation (Estimated: [1-2 hours])
- [ ] Update Javadoc
- [ ] Update README if needed
- [ ] Add code comments
- [ ] Document configuration

## Technical Approach

### Architecture Changes
- [Architecture decision 1]
- [Architecture decision 2]

### Design Patterns
- [Pattern 1 - e.g., Strategy, Factory, Observer]
- [Pattern 2]

### Code Organization
```
reactive-websockets-model/src/main/java/org/elpis/reactive/websockets/
├── event/model/          # New/modified interfaces
├── web/annotation/       # New/modified annotations
└── exception/            # New/modified exceptions

reactive-websockets-starter/src/main/java/org/elpis/reactive/websockets/
├── event/manager/        # Event manager implementations
├── handler/              # WebSocket handlers
├── config/               # Configuration classes
└── security/             # Security implementations

functional-tests/src/test/java/org/elpis/reactive/websockets/impl/
└── <feature>/            # Integration tests
```

## Testing Strategy

### Unit Tests
- Test classes: [List of test classes]
- Target coverage: 80%+
- Key scenarios:
  - [ ] Happy path
  - [ ] Error handling
  - [ ] Edge cases
  - [ ] Boundary conditions

### Integration Tests
- Test classes: [List of integration test classes]
- Test scenarios:
  - [ ] End-to-end feature flow
  - [ ] WebSocket connection
  - [ ] Event publishing/handling
  - [ ] Security integration

### Manual Testing
- [ ] Test in IDE
- [ ] Test with actual WebSocket client
- [ ] Test with different configurations
- [ ] Load testing (if applicable)

## Risk Assessment

### Potential Risks
| Risk | Impact | Mitigation |
|------|--------|-----------|
| [Risk 1] | [High/Medium/Low] | [Mitigation plan] |
| [Risk 2] | [High/Medium/Low] | [Mitigation plan] |

### Known Issues
- [Known issue 1 and workaround]
- [Known issue 2 and workaround]

## Dependencies & Blockers

### Internal Dependencies
- Depends on: [DEMO-456 - Other feature]
- Blocks: [DEMO-789 - Downstream feature]

### External Dependencies
- Library: [Version requirement]
- Configuration: [Required setup]

## Effort Estimation

| Phase | Estimated | Actual | Notes |
|-------|-----------|--------|-------|
| Planning | 2 hours | - | - |
| Implementation | 6 hours | - | - |
| Testing | 3 hours | - | - |
| Documentation | 1.5 hours | - | - |
| **Total** | **12.5 hours** | - | - |

## Timeline

- **Start Date:** [Date]
- **Target Completion:** [Date]
- **Deadline:** [Date]

## Sign-Off

- **Plan Created By:** [Developer Name]
- **Date Created:** [Date]
- **Status:** Draft / Approved
- **Reviewed By:** [Reviewer Name - if applicable]

## Updates

When extending this plan, add updates to this section:

### Update 1 - [Date]
- [What changed]
- [Why it changed]
- [Impact on timeline/effort]

### Update 2 - [Date]
- [What changed]
- [Why it changed]
- [Impact on timeline/effort]
```

### Plan Extension (If Needed)

If the plan needs to be extended during development:

1. Add an "Updates" section at the bottom
2. Document what changed and why
3. Update effort estimates
4. Update timeline if affected
5. Keep all original content for reference

**Never** delete or significantly rewrite the initial plan. Always append changes.

---

## Step 4: Create Development Branch

### Branch Naming Convention

**Format:** `<type>/{ISSUE_ID}-short-description`

**Type Options:**
- `feature/` - New feature implementation
- `bugfix/` - Bug fix
- `refactor/` - Code refactoring
- `docs/` - Documentation updates
- `test/` - Test improvements
- `perf/` - Performance optimization

### Examples

```bash
# Feature development
feature/DEMO-123-websocket-close-handling

# Bug fix
bugfix/DEMO-456-session-timeout-issue

# Refactoring
refactor/DEMO-789-event-manager-cleanup

# Documentation
docs/DEMO-321-api-documentation

# Test improvement
test/DEMO-654-coverage-improvement
```

### Branch Creation Steps

**Step 1: Ensure main branch is up to date**
```bash
git checkout main
git pull origin main
```

**Step 2: Create feature branch**
```bash
git checkout -b feature/DEMO-123-short-description
```

**Step 3: Verify branch creation**
```bash
git branch -a
git log --oneline -5
```

**Step 4: Push branch to remote**
```bash
git push -u origin feature/DEMO-123-short-description
```

### Branch Naming Rules

**Do:**
- ✓ Use lowercase letters
- ✓ Use hyphens to separate words
- ✓ Include issue ID
- ✓ Keep description short (2-4 words)
- ✓ Use descriptive names

**Don't:**
- ✗ Use uppercase letters
- ✗ Use underscores
- ✗ Use special characters
- ✗ Create branches from develop (use main)
- ✗ Use generic names like "fix" or "update"

### Wrong Examples
```
❌ feature/demo-123                    # No description
❌ feature/DEMO-123-websocket          # Uppercase
❌ feature/demo_123_websocket          # Underscores
❌ feature/demo-123-very-long-description-that-is-too-detailed
❌ Fix_Bug_123                         # Wrong format
```

---

## Step 5: Execute Development

### Development Checklist

Before starting code, ensure:

- ✓ YouTrack ticket retrieved and validated
- ✓ Development plan created
- ✓ Feature branch created
- ✓ IDE configured with code formatting
- ✓ Understanding of requirements complete

### Development Guidelines

Follow these guidelines during implementation:

1. **Code Style**
   - Use Google Java Format
   - See `code-style.instructions.md`

2. **Package Structure**
   - Place code in correct module and package
   - See `code-style.instructions.md`, Section 4

3. **Testing**
   - Write unit tests (80%+ coverage)
   - Write integration tests if applicable
   - See `testing.instructions.md`

4. **Commits**
   - Use descriptive commit messages
   - Reference ticket ID: `DEMO-123: Add feature description`
   - Keep commits focused and atomic
   - Commit frequently

### Example Commit Messages

```bash
# Good commit messages
git commit -m "DEMO-123: Implement WebSocket close handler"
git commit -m "DEMO-123: Add close status verification tests"
git commit -m "DEMO-123: Update handler configuration"
git commit -m "DEMO-123: Add Javadoc for new methods"

# Bad commit messages
❌ git commit -m "fixes"
❌ git commit -m "update code"
❌ git commit -m "demo-123"
❌ git commit -m "work in progress"
```

### Incremental Development

Develop in phases:

1. **Phase 1:** Create interfaces/models in model module
2. **Phase 2:** Implement core functionality
3. **Phase 3:** Add configuration and integration
4. **Phase 4:** Write comprehensive tests
5. **Phase 5:** Update documentation

Commit after each phase.

---

## Step 6: Create/Update Summary

### Summary File Structure

**Location:** `summary/{YOUTRACK_ISSUE_ID}/summary.md`

**Example:** `summary/DEMO-123/summary.md`

### When to Create/Update Summary

**Create Summary When:**
- Feature development is complete
- All tests pass
- Ready for code review
- Preparing pull request

**Update Summary When:**
- Plan changes significantly
- Implementation differs from plan
- Issues discovered during development
- Additional work identified

### Summary Template

```markdown
# Feature Development Summary: [ISSUE_ID]

## Overview
- **Issue ID:** [DEMO-123]
- **Summary:** [Feature title]
- **Status:** [In Progress / Ready for Review / Complete]
- **Branch:** feature/DEMO-123-short-description
- **Developer:** [Name]
- **Started:** [Date]
- **Completed:** [Date if done]

## What Was Implemented

### Key Features
1. [Feature 1 implemented]
2. [Feature 2 implemented]
3. [Feature 3 implemented]

### Files Created/Modified

#### New Files
- `path/to/NewClass.java` - [Description]
- `path/to/NewTest.java` - [Description]

#### Modified Files
- `path/to/ModifiedClass.java` - [Changes description]
- `path/to/ModifiedTest.java` - [Changes description]

### Classes/Interfaces Added

| Class | Type | Package | Purpose |
|-------|------|---------|---------|
| [ClassName] | Interface/Class | [org.elpis...] | [Purpose] |

## Implementation Details

### Architecture Decisions
- [Decision 1 and rationale]
- [Decision 2 and rationale]

### Key Implementation Points
- [Implementation detail 1]
- [Implementation detail 2]
- [Implementation detail 3]

### Configuration Changes
- [Configuration change 1]
- [Configuration change 2]

## Testing Summary

### Unit Tests
- Test classes created: [Count]
- Test methods: [List or count]
- Coverage: [Percentage]%
- Status: ✓ All passing

### Integration Tests
- Test classes created: [Count]
- Test methods: [List or count]
- Coverage: [Percentage]%
- Status: ✓ All passing

### Test Results
```
Tests run: [N]
Failures: 0
Errors: 0
Skipped: 0
Success rate: 100%
Coverage: [XX]%
```

## Acceptance Criteria Verification

- [x] Criterion 1 - [Evidence/Description]
- [x] Criterion 2 - [Evidence/Description]
- [x] Criterion 3 - [Evidence/Description]

## Changes from Plan

### What Matched Plan
- [What was implemented as planned]
- [What matched effort estimates]

### What Differed from Plan

| Original Plan | Actual Implementation | Reason |
|---------------|----------------------|--------|
| [Plan detail] | [What actually happened] | [Why] |

### Effort Summary

| Phase | Planned | Actual | Variance | Notes |
|-------|---------|--------|----------|-------|
| Planning | 2h | 2h | 0% | - |
| Implementation | 6h | 7h | +1h | More complex than expected |
| Testing | 3h | 4h | +1h | Found edge cases |
| Documentation | 1.5h | 1.5h | 0% | - |
| **Total** | **12.5h** | **14.5h** | **+2h** | - |

## Code Quality

### Code Review Checklist
- [x] Code follows Google Java Format
- [x] Package structure is correct
- [x] Naming conventions followed
- [x] Javadoc is complete
- [x] No code duplication
- [x] Error handling is proper
- [x] No security issues

### Coverage Report
```
Line Coverage: [XX]%
Branch Coverage: [XX]%
Critical Paths: [XX]%
Test Quality: ✓ Passed review
```

### Code Style Check
```
Formatting: ✓ Pass
Imports: ✓ Organized
Complexity: ✓ Acceptable
Duplication: ✓ None
Security: ✓ No issues
```

## Dependencies & Integration

### External Dependencies
- None added
- [New dependency 1] - [Reason]

### Integration Points
- Integrated with [Module/Class 1]
- Integrated with [Module/Class 2]
- Uses [Framework/Library 1]

### Backward Compatibility
- ✓ Fully backward compatible
- ⚠ Minor breaking changes: [Description]
- ✗ Major breaking changes: [Description]

## Known Limitations

- [Limitation 1]
- [Limitation 2]
- [Future improvement needed: Description]

## Documentation

### Javadoc
- ✓ All public classes documented
- ✓ All public methods documented
- ✓ Complex logic explained
- ✓ Examples provided where needed

### README Updates
- ✓ Updated if needed
- [Section updated: Description]

### Code Comments
- ✓ Complex logic explained
- ✓ Business logic documented
- ✓ No over-commenting

## Next Steps

### Immediate (Before Merge)
- [ ] Code review approval
- [ ] All tests passing
- [ ] Coverage target met
- [ ] Documentation complete

### Post-Merge
- [ ] Deploy to staging
- [ ] Integration testing
- [ ] User acceptance testing
- [ ] Deploy to production

## Related Issues/PRs

- Depends on: [DEMO-456]
- Blocks: [DEMO-789]
- Related PR: #[PR_NUMBER]
- Closes: #[GITHUB_ISSUE]

## Sign-Off

- **Implemented By:** [Developer Name]
- **Completed Date:** [Date]
- **Ready for Review:** [Yes/No]
- **Code Review Status:** [Pending/Approved]

## Appendix

### Build Output
```
[Maven build output summary]
```

### Test Output
```
[Test execution output]
```

### Coverage Report Link
[Link to coverage report]
```

### Summary Extension (If Needed)

If the summary needs to be extended during review or after feedback:

1. Add an "Updates" section
2. Document what changed and why
3. Update test results if changed
4. Keep all original content
5. Mark items as updated

**Update Template:**
```markdown
## Updates (Post-Review)

### Update 1 - [Date]
**Feedback:** [What feedback was provided]
**Changes Made:** [What was changed]
**Result:** [Outcome of changes]

### Update 2 - [Date]
**Feedback:** [What feedback was provided]
**Changes Made:** [What was changed]
**Result:** [Outcome of changes]
```

---

## Agent Instructions

### Critical Rules for Agents

**RULE 1: PLAN FIRST, EXECUTE SECOND**
- Always create plan BEFORE writing code
- Understand requirements thoroughly
- Identify all affected areas
- Never skip planning phase

**RULE 2: NEVER CREATE SUMMARY UNLESS REQUESTED**
- Only create summary when explicitly requested by developer
- Don't generate summary files proactively
- Wait for developer instruction

**RULE 3: UPDATE ONLY PLAN OR SUMMARY**
- If extending plan: Add to plan file only
- If updating summary: Update summary file only
- Never create new auxiliary files
- Never delete or rewrite original content

**RULE 4: VALIDATE BEFORE PROCEEDING**
- Retrieve ticket from YouTrack
- Check ticket has ID, summary, description, type, status
- Reject invalid tickets immediately
- Provide clear rejection reasons

**RULE 5: PRESERVE HISTORY**
- Keep all original content
- Add updates in dedicated sections
- Document what changed and why
- Maintain plan/summary as living documents

### Agent Workflow

**When Developer Says: "Implement DEMO-123"**

1. **VALIDATE**
   ```
   Get ticket from YouTrack
   Check: ID, summary, description, type, status
   Reject if invalid with clear reason
   ```

2. **PLAN**
   ```
   Create plan/{ISSUE_ID}/plan.md
   Analyze requirements
   Identify affected modules
   Outline implementation approach
   Estimate effort
   Plan testing strategy
   ```

3. **CONFIRM**
   ```
   Show plan to developer
   Wait for approval/feedback
   Update plan if needed
   Get approval to proceed
   ```

4. **EXECUTE**
   ```
   Create feature branch
   Write code following guidelines
   Write tests simultaneously
   Commit frequently
   Keep plan/summary updated if significant changes
   ```

5. **COMPLETE**
   ```
   Run all tests
   Generate coverage report
   Verify code formatting
   Create/update summary if requested
   Prepare for pull request
   ```

### Agent Decision Tree

```
Developer Request
    ↓
[Has YouTrack Issue ID?] ──→ NO → Ask for issue ID
    ↓ YES
[Retrieve ticket]
    ↓
[Ticket valid?] ──→ NO → Reject with specific reasons
    ↓ YES
[Create plan/{ID}/plan.md]
    ↓
[Show plan to developer]
    ↓
[Approved?] ──→ NO → Update and reshow
    ↓ YES
[Create feature branch]
    ↓
[Implement feature] ──→ Update plan if major changes
    ↓
[All tests pass?] ──→ NO → Debug and fix
    ↓ YES
[Coverage ≥ 80%?] ──→ NO → Add more tests
    ↓ YES
[Developer requests summary?] ──→ YES → Create/update summary/{ID}/summary.md
    ↓ NO
[Ready for PR] ──→ Wait for next instruction
```

### Agent-Developer Communication Templates

**Invalid Ticket Template:**
```
❌ TICKET VALIDATION FAILED

Issue: [ID]
Summary: [Summary if available]

❌ Reason: [Specific reason]

Required information:
- [Missing item 1]
- [Missing item 2]

Please update the ticket and resubmit.
```

**Plan Ready for Review:**
```
✅ DEVELOPMENT PLAN CREATED

Issue: DEMO-123
Plan Location: plan/DEMO-123/plan.md

📋 Plan Summary:
- Affected Modules: [List]
- Estimated Effort: [Hours]
- Key Phases: [List]
- Testing Strategy: [Summary]
- Risks: [List]

📌 Next Steps:
Please review the plan and provide:
1. Approval to proceed
2. Any concerns or changes needed
3. Priority/timeline adjustments if needed

Ready to implement once approved.
```

**Implementation Complete:**
```
✅ FEATURE IMPLEMENTATION COMPLETE

Issue: DEMO-123
Branch: feature/DEMO-123-short-description

📊 Completion Summary:
- Tests Passing: [Count] / [Total]
- Coverage: [X]%
- Files Created: [Count]
- Files Modified: [Count]
- Commits: [Count]

✓ Ready for:
- Code review
- Testing
- Pull request

Would you like me to:
1. Create summary file?
2. Create pull request?
3. Make additional changes?
```

---

## File Structure

### Directory Organization

```
project-root/
├── plan/                                # Development plans
│   ├── DEMO-123/
│   │   └── plan.md                      # Development plan for DEMO-123
│   ├── DEMO-456/
│   │   └── plan.md
│   └── ...
│
├── summary/                             # Development summaries
│   ├── DEMO-123/
│   │   └── summary.md                   # Summary for DEMO-123
│   ├── DEMO-456/
│   │   └── summary.md
│   └── ...
│
├── .github/
│   └── instructions/
│       ├── code-style.instructions.md
│       ├── testing.instructions.md
│       └── feature-development.instructions.md
│
└── src/
    ├── reactive-websockets-model/
    ├── reactive-websockets-annotation-processor/
    ├── reactive-websockets-starter/
    ├── functional-tests/
    └── report-aggregate/
```

### File Naming

**Plan Files:**
```
plan/{ISSUE_ID}/plan.md

Examples:
- plan/DEMO-123/plan.md
- plan/PROJ-456/plan.md
- plan/BUG-789/plan.md
```

**Summary Files:**
```
summary/{ISSUE_ID}/summary.md

Examples:
- summary/DEMO-123/summary.md
- summary/PROJ-456/summary.md
- summary/BUG-789/summary.md
```

### File Lifespan

**Plan Files:**
- Created at start of feature development
- Maintained throughout development
- Updated if requirements change
- Preserved as historical record after completion

**Summary Files:**
- Created when feature is ready for review
- Updated during code review if feedback requires changes
- Preserved as implementation record
- Referenced in pull request

---

## Best Practices

### 1. **Validate Early**
- Always validate ticket before spending time
- Catch issues early (missing requirements, unclear scope)
- Communicate blockers immediately

### 2. **Plan Thoroughly**
- Don't rush the planning phase
- Identify all affected areas
- Consider testing approach upfront
- Plan for edge cases

### 3. **Communicate Updates**
- If plan changes significantly, update plan file
- Explain why changes were needed
- Keep developer informed
- Don't make silent assumptions

### 4. **Implement Incrementally**
- Code in phases matching plan
- Commit after each significant change
- Write tests as you go
- Don't accumulate large commits

### 5. **Document as You Go**
- Add Javadoc while coding
- Update plan if needed
- Add code comments for complex logic
- Don't leave documentation for last

### 6. **Test Comprehensively**
- Write unit tests for all new code
- Write integration tests for feature
- Achieve 80%+ coverage target
- Test error paths, not just happy path

### 7. **Preserve Plan History**
- Keep original plan content
- Add updates in dedicated section
- Maintain chronological order
- Reference back to original

### 8. **Communicate Results**
- Share plan with developer
- Get approval before proceeding
- Report completion status
- Ask if summary is needed

---

## Troubleshooting

### Issue: Ticket Validation Fails

**Problem:** Ticket is missing required information

**Solution:**
1. Identify specific missing information
2. Communicate clearly what's needed
3. Don't proceed with development
4. Request ticket update in YouTrack

**Example:**
```
❌ Cannot proceed with DEMO-123
Missing required information:
- Description is empty (required)
- No acceptance criteria defined
- Type field is not set

Please update the ticket with:
1. Detailed description of requirements
2. Clear acceptance criteria
3. Type (Bug/Task/Feature/Improvement)

Resubmit when complete.
```

### Issue: Plan is Too Vague

**Problem:** Plan doesn't have enough detail to start coding

**Solution:**
1. Add more analysis to plan
2. Break down tasks further
3. Identify specific files/classes
4. Get feedback from developer

**Update Plan:**
```
## Updates

### Update 1 - [Date]
**Issue:** Initial plan was too high-level
**Changes:** 
- Added specific class/file names
- Broke implementation into sub-tasks
- Added detailed testing scenarios
**Impact:** More concrete development approach
```

### Issue: Significant Plan Deviation

**Problem:** Implementation differs significantly from plan

**Situation Examples:**
- Found requirement was incomplete
- Technical approach had to change
- Discovered additional work needed
- Effort estimate was way off

**Solution:**
1. Update plan with what changed
2. Document why change was necessary
3. Estimate impact on timeline
4. Communicate to developer
5. Get approval before continuing

**Update Plan:**
```
## Updates

### Update 1 - [Date]
**Issue:** [What was discovered]
**Original Plan:** [What was planned]
**New Approach:** [What will actually be done]
**Reason:** [Why change was needed]
**Timeline Impact:** [+X hours due to Y reason]
**Developer Approval:** [Yes/No - pending]
```

### Issue: Test Coverage Below Target

**Problem:** Coverage is below 80% target

**Solution:**
1. Identify uncovered code paths
2. Write additional tests
3. Focus on critical paths first
4. Document coverage in summary

**Steps:**
```bash
1. Generate coverage report
   mvn clean test jacoco:report

2. Open report
   open target/site/jacoco/index.html

3. Identify uncovered lines
4. Write tests for those paths
5. Re-run coverage
6. Verify target met
```

---

## References

- [YouTrack API Documentation](https://www.jetbrains.com/help/youtrack/standalone/youtrack-rest-api.html)
- [Google Java Format](https://github.com/google/google-java-format)
- [Git Workflow Best Practices](https://www.atlassian.com/git/tutorials)
- [Code Review Best Practices](https://google.github.io/eng-practices/review/)
- Related guides:
  - `code-style.instructions.md` - Code formatting rules
  - `testing.instructions.md` - Testing guidelines

---

**Last Updated:** December 8, 2025

**Status:** ✅ Complete and Ready for Use

**Usage:** Developers and AI agents should reference this guide when starting new features. Agents must follow the rules in Section 9 (Agent Instructions).

