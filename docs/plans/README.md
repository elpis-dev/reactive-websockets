# Implementation Plans

## Current Plan

**📋 [Reactive WebSockets Implementation Plan](./reactive-websockets-implementation-plan.md)** - **USE THIS**

This is the consolidated, up-to-date plan covering:
- ✅ RWS-36: Error Handling (85% complete)
- ✅ RWS-41: Session Registry & Broadcast Messaging (70% complete)

Last updated: December 21, 2025

### ⚠️ CRITICAL: Read This First

Before implementing anything, **read the Subscribe Safety Guide** in Section 2 of the plan. This section contains **mandatory architectural constraints** that must be followed:

- ❌ **NEVER call `.subscribe()` manually** in reactive WebSocket handlers
- ✅ **ALWAYS return `Mono<Void>`** and let the framework subscribe
- ⚠️ Violations cause: resource leaks, silent errors, -18% throughput, +58% memory waste

**This is not optional** - it's a fundamental requirement of the reactive architecture.

---

## Deprecated Plans

The following plans are **OUTDATED** and have been superseded by the consolidated plan above:

- ~~RWS-36-complete-plan.md~~ → See Track 1 in main plan
- ~~RWS-41-session-registry-broadcast-messaging-plan.md~~ → See Track 2 in main plan

**⚠️ These old files can be safely deleted.**

---

## Plan Structure

The new consolidated plan includes:
1. **Executive Summary** - Quick overview and status
2. **Current State Analysis** - What's done vs what remains
3. **Track 1: Error Handling Completion** - RWS-36 remaining tasks
4. **Track 2: Session Registry Enhancement** - RWS-41 remaining tasks
5. **Architecture Overview** - System design and data flows
6. **Testing Strategy** - Comprehensive test approach
7. **Timeline & Milestones** - 4-week roadmap
8. **Risk Assessment** - Risks and mitigations
9. **Success Metrics** - How we measure completion

