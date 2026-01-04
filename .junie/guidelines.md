# Junie's Operational Guidelines for The Expat Ledger

This document consolidates Junie's operational rules and the general guidelines for development agents. It serves as the primary reference for Junie's behavior in this project.

---

## 1. Core Operational Rules (from .junierules)

### 1.1 Scope Control

- **NO unauthorized scope changes**: Junie MUST NOT change the project's core purpose, tech stack, or architectural style without an explicit `<issue_update>` from the user.
- **ADR-First**: Any significant architectural or technical change must be documented in a new ADR under `docs/architecture/decisions/` before implementation.
- **Blueprint Compliance**: All development must align with the "Master Blueprint: The Expat Ledger".

### 1.2 Tech Stack Consistency

- **Language**: Scala 3 (using Scala-native features where possible: enums, opaque types, etc.).
- **Backend**: Pure Scala-native frameworks (e.g., Cats Effect, Http4s, Doobie/Skunk). Discard Spring Boot to avoid interoperability issues.
- **Type Safety**: Use refined types for configuration where possible (e.g., `com.comcast.ip4s.Host` and `com.comcast.ip4s.Port` for network addresses).
- **Configuration**: Use `pureconfig` for property-based configuration loading, with support for refined types (e.g., `pureconfig-ip4s`).
- **API**: Protobuf/gRPC for internal sync, CloudEvents 1.0 for async.
- **Build**: sbt (Scala Build Tool).

### 1.3 Documentation & Backlog

- **Backlog-as-Code**: Always update the JSON status in `docs/backlog/iteration-N.json` and run `python3 scripts/backlog_render.py` after completing a task.
- **Changelogs**: Maintain `docs/governance/scope-CHANGELOG.md` for scope changes and `docs/contracts/CHANGELOG.md` for API changes.
- **Traceability**: Every PR/Change should reference the functional requirement (e.g., FR-1) it addresses.

### 1.4 Quality Standards

- **Test Coverage**: 100% coverage for domain logic.
- **Naming**: Follow Scala 3 naming conventions (camelCase).
- **Security**: Always consider tenant isolation and JWT scoping.
- **IDs & Time**: Use **UUID v4** for primary keys and **UTC** for all timestamps (ISO-8601).
- **Idempotency**: Use `Idempotency-Key` for all mutating endpoints.
- **Tenant Isolation**: Every read/write must be filtered by `tenant_id`.

### 1.5 Deployment

- **Docker-First**: Ensure all services are containerizable and work within the `docker-compose.app.yml` stack.

---

## 2. Operating Manual for Development Agents (from AGENTS.md)

### 2.1 Principles & Goals

- **API‑first:** deliver a complete, secure, and stable API before UI.
- **Contract discipline:** API/event changes are explicit, versioned, and documented.
- **Security by default:** OWASP ASVS L3, least privilege, tenant isolation.
- **Observability:** traces, metrics, and structured JSON logs are part of “done”.
- **Docs‑as‑code:** ADRs, C4, OpenAPI, and governance live under `docs/`.

**SLO targets** (steady‑state):

- P95 latency < **200 ms**
- Error rate < **0.1%**
- MTTR < **30 min**

### 2.2 Repository Facts

- **Language/Build:** Scala 3, Cats Effect, Http4s, **sbt**.
- **CI:** GitHub Actions uses `sbt test`.
- **Tooling:** `pre-commit`, commitlint (Node hook), Prettier (Node hook), Scalafmt.
- **Docs:** MkDocs; `mkdocs.yml` uses Python constructor tags and is **excluded** from the YAML check.
- **Contracts:** `docs/contracts/openapi/v1/openapi.yaml` (+ `CHANGELOG.md`).
- **ADRs:** `docs/architecture/decisions/`
- **Governance:** `docs/governance/`
- **Deployment notes:** `docs/ops/deployment-render.md`

### 2.3 Roles & Responsibilities

#### Code Generation Agent (Codex/Junie)

- Implements small, well‑scoped changes in **Scala 3** using functional paradigms.
- Writes/updates tests (unit/integration). Use **Testcontainers** for DB.
- Ensures tenant scoping, idempotency, UTC time, data validation, pagination defaults.
- Runs locally before PR: `sbt test` → `pre-commit run --all-files`.

#### Test Agent

- Maintains test coverage (goal ≥ 70% core logic) and **no flaky tests**.
- Provides deterministic seeds/fixtures (e.g., Excel samples).
- Verifies idempotency, historical FX conversions (USD/EUR/COP), and **bank attribution** per transaction.

#### Docs/Contracts Agent

- When code changes API surface, **update both**:
  - `docs/contracts/openapi/v1/openapi.yaml` and `docs/contracts/openapi/v1/CHANGELOG.md`.
  - Relevant ADRs and `docs/governance/scope-CHANGELOG.md`.
- Keep C4 diagrams and docs consistent (do not break Mermaid behavior).

#### Security Agent

- Enforces tenant isolation at service/repository layers.
- Validates OAuth2 Resource Server (JWT) config (when present), CORS allow‑list, and rate limiting.
- Ensures PII redaction in logs and least‑privilege DB roles.

#### CI Agent

- Keeps `.github/workflows/ci.yml` aligned with sbt.
- Ensures CI runs: **Scala 3** setup → `sbt test` → `pre-commit run --all-files`.
- May add caching but **must not** weaken checks or skip tests.

### 2.4 Guardrails & Constraints

- **Security:** OWASP ASVS L3 posture; no secrets in repo; configs must be property-based (e.g., `application.conf`) and loaded via `pureconfig`.
- **IDs & Time:** UUID PKs; timestamps in **UTC**.
- **Tenant isolation:** every read/write must filter by `tenant_id`; cross‑tenant access returns 403/404 without data leak.
- **Idempotency:** mutating endpoints require `Idempotency-Key`; persist `(route, key, response_hash)`; ingestion dedupe on `(tenant_id, source_id)`.
- **Performance:** avoid N+1 queries; define critical indexes in ADR; respect pagination defaults.
- **Logging:** structured JSON; include correlation/tenant IDs; no secrets/PII.
- **Style:** formatting enforced by pre‑commit (Prettier + Scalafmt).

**Forbidden** (agents must never):

- Introduce external network calls at build/test time without ADR justification.
- Commit credentials, private keys, or production endpoints.
- Bypass pre‑commit/CI gates or force‑push to `main`.

### 2.5 Workflows

#### Feature or Bugfix

1. Create a branch: `feat/<scope>-<short>` or `fix/<scope>-<short>`.
2. Write tests → implement code → run `sbt test`.
3. Run `pre-commit run --all-files` and fix issues.
4. Open PR with **Conventional Commit** title.

#### Contract change (OpenAPI)

1. Update endpoint definition and **synchronize** `docs/contracts/openapi/v1/openapi.yaml`.
2. Append a new entry to `docs/contracts/openapi/v1/CHANGELOG.md`.
3. If change is breaking, create/extend an ADR and justify versioning (`/v2` when needed).

#### Database change

1. Add reversible Flyway migration.
2. Update ADR for indexes/structure if needed.
3. Add/adjust Testcontainers tests.

### 2.6 PR Quality Gate

- ✅ **Build/tests:** `sbt test` green.
- ✅ **pre-commit:** all hooks pass.
- ✅ **Docs/Contracts:** updated when API surface changed.
- ✅ **Security:** tenant isolation, headers, idempotency validated.
- ✅ **No secrets** in diffs; structured logs only.

---

## 3. Reference Commands

```bash
# Build & test
sbt test

# Repo hygiene
pre-commit run --all-files

# Backlog update
python scripts/backlog_render.py
```
