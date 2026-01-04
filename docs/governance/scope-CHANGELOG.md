# Scope CHANGELOG

## 2026-01-04 — Improved AvroSchemaLoader Error Handling

**Summary**

- Replaced generic `RuntimeException` with `java.io.FileNotFoundException` when an Avro schema file is missing in the classpath.
- Added unit tests for `AvroSchemaLoader`.

**Impacts**

- **Maintainability**: Better error context for debugging missing schema files.
- **Robustness**: Added test coverage for infrastructure messaging components.

**Actions**

- Modified `modules/shared-kernel/src/main/scala/com/expatledger/kernel/infrastructure/messaging/AvroSchemaLoader.scala`.
- Created `modules/shared-kernel/src/test/scala/com/expatledger/kernel/infrastructure/messaging/AvroSchemaLoaderSpec.scala`.
- Updated `docs/backlog/iteration-1.json` (TASK-15 marked as completed).

## 2026-01-04 — Refactored EventType to use Enumeratum

**Summary**

- Replaced hardcoded string-based `eventType` with an `enumeratum` enum `EventType`.
- Improved type safety in `OutboxEvent` and `DomainEvent`.
- Updated `RabbitMQPublisher` to use exhaustive pattern matching on `EventType`.
- Implemented a Skunk codec for `EventType` to handle database persistence.

**Impacts**

- **Type Safety**: Reduced risk of runtime errors due to typoed event names.
- **Maintainability**: Centralized event type definitions in `EventType` enum.
- **Interoperability**: Maintained string-based representation for external messaging (CloudEvents) while using rich types internally.

**Actions**

- Added `enumeratum` and `enumeratum-circe` to `project/Dependencies.scala`.
- Created `modules/shared-kernel/src/main/scala/com/expatledger/kernel/domain/events/EventType.scala`.
- Updated `DomainEvent` and `OutboxEvent` in `shared-kernel`.
- Updated `RabbitMQPublisher`, `OutboxRepositoryLive`, and `TenantCreated` in `tenant-service`.
- Fixed missing test dependencies in `apiGatewayDependencies` and `tenantServiceDependencies`.

## 2026-01-03 — Outbox Poller Enhanced (Error Handling & Logging)

**Summary**

- Refactored `OutboxPoller` to ensure stack safety by replacing recursive restarts with fs2's `.repeat`.
- Integrated `log4cats` with `slf4j` and `logback` for structured logging across the project.
- Implemented exponential backoff retries for outbox event publishing.

**Impacts**

- **Reliability**: Improved poller stability for long-running processes; reduced risk of `StackOverflowError`.
- **Observability**: Structured JSON-ready logging enabled.
- **Resilience**: Added retries for transient messaging failures.

**Actions**

- Updated `project/Dependencies.scala` with `log4cats` and `logback`.
- Refactored `modules/tenant-service/src/main/scala/com/expatledger/tenants/application/OutboxPoller.scala`.
- Created `modules/tenant-service/src/test/scala/com/expatledger/tenants/application/OutboxPollerTest.scala`.
- Updated `docs/backlog/iteration-1.json` (T1.22 marked as done).

## 2026-01-03 — Pre-commit Hook Standardized

**Summary**

- Standardized pre-commit hook execution to use `python -m pre_commit` to support `asdf` environments.
- Removed obsolete `google-java-format` hook (project is Scala-only).
- Updated `README.md` and `Makefile` to reflect standardized setup.

**Impacts**

- **Developer Experience**: Improved first-time environment setup reliability.
- **Repository Hygiene**: Removed unused formatting scripts and configurations.

**Actions**

- Updated `.pre-commit-config.yaml` (removed `google-java-format`).
- Updated `Makefile` (standardized `lint` and `format` targets).
- Updated `README.md` (updated prerequisites and setup instructions).
- Updated `docs/backlog/iteration-1.json` with task T1.23.

## 2026-01-02 — Guidelines Consolidation

**Summary**

- Consolidated `.junierules` and `docs/governance/AGENTS.md` into `.junie/guidelines.md`.
- Established a single source of truth for Junie's operational rules and agent guidelines.

**Impacts**

- **Documentation**: `.junie/guidelines.md` is now the primary reference for agents.
- **Repository Hygiene**: Removed redundant `.junierules` and `AGENTS.md`.

**Actions**

- Removed `.junierules`.
- Removed `docs/governance/AGENTS.md`.
- Updated `mkdocs.yml` and `README.md` to point to `.junie/guidelines.md`.

## 2026-01-02 — Build Tool Pivot: sbt adoption

**Summary**

- Replaced **Gradle** with **sbt** as the primary build tool for the Scala 3 project.
- Aligned with Scala ecosystem standards and user preference.

**Impacts**

- **Build**: sbt (Scala Build Tool) used for compilation, testing, and formatting.
- **Documentation**: README, AGENTS.md, and .junierules updated to reflect sbt usage.
- **Backlog**: Tasks updated to reflect sbt setup.

**Actions**

- ADR-010 created: Use sbt as the Build Tool.
- `README.md` and `docs/governance/AGENTS.md` updated.
- `.junierules` updated to enforce sbt.
- `docs/backlog/iteration-0.json` updated with sbt tasks.

## 2026-01-02 — Scala-Native Pivot & Name Correction

**Summary**

- Corrected project name to **The Expat Ledger**.
- Pivot to **Scala-native frameworks** (discarding Spring Boot) to avoid interoperability friction.
- Adopted Typelevel/ZIO stacks for service implementation.

**Impacts**

- **Language**: Pure Scala 3 with functional effect systems (Cats Effect/ZIO).
- **Architecture**: Distributed Modular Monolith using Scala-native libraries for Gateway and Discovery.
- **Documentation**: Updated README, ADRs, and .junierules to reflect pure Scala stack.
- **Tech Stack**: Scala 3, Cats Effect, Http4s, Doobie/Skunk, fs2-grpc.

**Actions**

- ADR-009 updated: Transition to Scala 3 and Native Frameworks.
- `docs/architecture/scala-frameworks.md` updated with pure Scala recommendations.
- `README.md` and `.junierules` updated (removed NomadLedger and Spring Boot).

## 2026-01-02 — NomadLedger Pivot & Scala 3 Transition (Superseded)

## 2025-09-29 — Bank attribution + multi-currency balances

**Summary**

- Add bank identifier on every transaction (FR-10).
- Maintain running balances in EUR and COP (FR-11).

**Impacts**

- Contracts: +`bankId` in Transaction resource (backward-compatible).
- Data: new `bank` table and `bank_id` FK in `transaction`.
- Security/Privacy: no new PII; ensure bank names are tenant-scoped.
- Operations: FX cache must cover USD/EUR/COP by date.

**Actions**

- ADR-005 created to justify schema & projection approach.
- Migrations added (e.g., `V5__bank_and_tx_fk.sql`).
- OpenAPI updated: `/v1/transactions` includes `bankId`.

## 2025-09-29 — Hosting on Render (Free tier) + UX design workflow (Stitch + Figma)

**Summary**

- Decide to deploy API (Docker) and Web (Next.js SSR) on **Render Free** for public demos.
- Establish **Stitch → Figma** pipeline for UI generation + storyboard.

**Impacts**

- **Ops**: Free Postgres has ~1 GB and **expires ~30 days** → add seed/export scripts; non-prod only.
- **SLOs**: Free web services may idle; exclude **first-hit-after-idle** from latency SLO, track separately.
- **Security**: Keep secrets in Render dashboard; no prod data.
- **Docs**: Add `render.yaml`, ADR-007, and `docs/ops/deployment-render.md`; add `docs/ux/stitch-prompt.md` with the prompt.

**Actions**

- ADR-007 accepted (Render hosting decision & mitigations).
- Added `render.yaml` blueprint at repo root.
- Updated `docs/governance/SLOs-SLIs.md` with cold-start note and metric.
- Created `docs/ops/deployment-render.md` and `docs/ux/stitch-prompt.md`.
