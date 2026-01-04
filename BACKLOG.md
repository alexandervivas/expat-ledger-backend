# Project Backlog

## Iteration 0: Infrastructure & Foundation ⏳

| ID | Task | Status | Owner |
| :--- | :--- | :--- | :--- |
| T0.1 | Setup multi-module sbt project structure | ✅ Done | Junie |
| T0.2 | Define 'Money' Opaque Types in Shared Kernel | ✅ Done | Junie |
| T0.3 | Scaffold API Gateway with Tapir, Http4s & Cats Effect | ✅ Done | Junie |
| T0.4 | Docker Compose 'Expat Stack' v1 | ✅ Done | Junie |
| T0.5 | Enforce ip4s Host and Port Validation | ✅ Done | Junie |

## Iteration 1: The Expat Identity 🚀

| ID | Task | Status | Owner |
| :--- | :--- | :--- | :--- |
| T1.1 | Setup Skunk Persistence & Flyway | ✅ Done | Junie |
| T1.2 | Implement Outbox Pattern Infrastructure | ✅ Done | Junie |
| T1.3 | Implement 'Onboard Tenant' Use Case | ✅ Done | Junie |
| T1.4 | Outbox Poller & CloudEvent Publisher | ✅ Done | Junie |
| T1.5 | Tenant API gRPC & Gateway Bridge | ✅ Done | Junie |
| T1.6 | Persistence Integration Test (Testcontainers) | ⏳ Todo | Junie |
| T1.7 | Consolidate Guidelines | ✅ Done | Junie |
| T1.8 | Rich Event Model Refactoring | ✅ Done | Junie |
| T1.9 | Optimize Outbox Batch Inserts | ✅ Done | Junie |
| T1.10 | Fix brittle eventType derivation | ✅ Done | Junie |
| T1.11 | Fix Python Version in .tool-versions | ✅ Done | Junie |
| T1.13 | Optimize OutboxRepository saveAll performance | ✅ Done | Junie |
| T1.15 | Fix Skunk transactional compilation error | ✅ Done | Junie |
| T1.16 | Enforce 'One Entity Per File' Rule | ✅ Done | Junie |
| T1.17 | Enforce DDD & Hexagonal Architecture | ✅ Done | Junie |
| T1.18 | Separate Service Contract from Implementation | ✅ Done | Junie |
| T1.19 | Implement Dependency Injection with Scala 3 Contextual Abstractions | ✅ Done | Junie |
| T1.20 | Standardize Dependency Injection with Google Guice | ✅ Done | Junie |
| T1.21 | Batch Insert Tenant Tax Residencies | ✅ Done | Junie |
| T1.22 | Enhance Outbox Poller Error Handling & Logging | ✅ Done | Junie |
| T1.23 | Fix and Standardize Pre-commit Hook Setup | ✅ Done | Junie |
| T1.24 | Decouple Event Schemas from Code | ✅ Done | Junie |
| TASK-12 | Remove owner concept from tenant creation | ⏳ Todo | Junie |
| TASK-13 | Fix tenant-service test dependencies | ⏳ Todo | Junie |
| TASK-14 | Refactor EventType to use Enumeratum | ⏳ Todo | Junie |

