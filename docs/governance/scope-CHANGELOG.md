# Scope CHANGELOG

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
