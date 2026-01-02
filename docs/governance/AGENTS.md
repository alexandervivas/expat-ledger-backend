# AGENTS.md — Operating Manual for Development Agents

This document defines how automated and AI agents (e.g., Codex, linters, bots) participate in the **Personal Banking API** development lifecycle. It sets roles, workflows, guardrails, and quality gates so every agent action moves the project in the intended direction.

> **Scope:** API‑only repository (Java 21, Spring Boot, **Gradle**, PostgreSQL). Frontend is out of scope for current iterations.

---

## 1) Principles & Goals

- **API‑first:** deliver a complete, secure, and stable API before UI.
- **Contract discipline:** API/event changes are explicit, versioned, and documented.
- **Security by default:** OWASP ASVS L3, least privilege, tenant isolation.
- **Observability:** traces, metrics, and structured JSON logs are part of “done”.
- **Docs‑as‑code:** ADRs, C4, OpenAPI, and governance live under `docs/`.

**SLO targets** (steady‑state, Render Free cold‑start excluded):

- P95 latency < **200 ms**
- Error rate < **0.1%**
- MTTR < **30 min**

---

## 2) Repository Facts (for agents)

- **Language/Build:** Java 21, Spring Boot, **Gradle**.
- **CI:** GitHub Actions uses `gradle/actions/setup-gradle@v3` with a pinned version (no wrapper committed).
- **Tooling:** `pre-commit`, commitlint (Node hook), Prettier (Node hook), Google Java Format (local JAR script `scripts/google-java-format.sh`).
- **Docs:** MkDocs; `mkdocs.yml` uses Python constructor tags and is **excluded** from the YAML check.
- **Contracts:** `docs/contracts/openapi/v1/openapi.yaml` (+ `CHANGELOG.md`).
- **ADRs:** `docs/architecture/decisions/`
- **Governance:** `docs/governance/`
- **Deployment notes:** `docs/ops/deployment-render.md`

---

## 3) Roles & Responsibilities (Agents)

### 3.1 Code Generation Agent (Codex)

- Implements small, well‑scoped changes in Java 21 with Spring Boot.
- Writes/updates tests (unit/integration). Use **Testcontainers** for DB.
- Ensures tenant scoping, idempotency, UTC time, bean validation, pagination defaults.
- Runs locally before PR: `gradle test` → `pre-commit run --all-files`.

### 3.2 Test Agent

- Maintains test coverage (goal ≥ 70% core logic) and **no flaky tests**.
- Provides deterministic seeds/fixtures (e.g., Excel samples).
- Verifies idempotency, historical FX conversions (USD/EUR/COP), and **bank attribution** per transaction.

### 3.3 Docs/Contracts Agent

- When code changes API surface, **update both**:

  - `docs/contracts/openapi/v1/openapi.yaml` and `docs/contracts/openapi/v1/CHANGELOG.md`.
  - Relevant ADRs and `docs/governance/scope-CHANGELOG.md`.

- Keep C4 diagrams and docs consistent (do not break Mermaid behavior).

### 3.4 Security Agent

- Enforces tenant isolation at service/repository layers.
- Validates OAuth2 Resource Server (JWT) config (when present), CORS allow‑list, and rate limiting.
- Ensures PII redaction in logs and least‑privilege DB roles.

### 3.5 CI Agent

- Keeps `.github/workflows/ci.yml` aligned with `setup-gradle`.
- Ensures CI runs: Java 21 setup → `gradle test` → `pre-commit run --all-files`.
- May add caching but **must not** weaken checks or skip tests.

### 3.6 Release/Deploy Agent (later)

- Manages tagging/release notes and Render deployment notes. **Never** auto‑merge to `main`.

> **Rule:** No agent pushes to `main` directly. Use PRs + human review.

---

## 4) Guardrails & Constraints

- **Security:** OWASP ASVS L3 posture; no secrets in repo; env‑based configs only.
- **IDs & Time:** UUID PKs; timestamps in **UTC** (`java.time`).
- **Tenant isolation:** every read/write must filter by `tenant_id`; cross‑tenant access returns 403/404 without data leak.
- **Idempotency:** mutating endpoints require `Idempotency-Key`; persist `(route, key, response_hash)`; ingestion dedupe on `(tenant_id, source_id)`.
- **Performance:** avoid N+1 queries; define critical indexes in ADR; respect pagination defaults.
- **Logging:** structured JSON; include correlation/tenant IDs; no secrets/PII.
- **Style:** formatting enforced by pre‑commit (Prettier + Google Java Format script).

**Forbidden** (agents must never):

- Introduce external network calls at build/test time without ADR justification.
- Commit credentials, private keys, or production endpoints.
- Bypass pre‑commit/CI gates or force‑push to `main`.

---

## 5) Workflows (What agents do step‑by‑step)

### 5.1 Feature or Bugfix

1. Create a branch: `feat/<scope>-<short>` or `fix/<scope>-<short>`.
2. Write tests → implement code → run `gradle test`.
3. Run `pre-commit run --all-files` and fix issues.
4. Open PR with **Conventional Commit** title and the PR template checklist.

### 5.2 Contract change (OpenAPI)

1. Update controller/DTOs and **synchronize** `docs/contracts/openapi/v1/openapi.yaml`.
2. Append a new entry to `docs/contracts/openapi/v1/CHANGELOG.md`.
3. If change is breaking, create/extend an ADR and justify versioning (`/v2` when needed).
4. PR must include examples and Problem Details per RFC7807.

### 5.3 Database change

1. Add reversible Flyway migration.
2. Update ADR for indexes/structure if needed.
3. Add/adjust Testcontainers tests to validate forward/backward migration.

### 5.4 Observability

1. Add metrics/traces/logs for new flows.
2. Ensure labels include `tenantId` (non‑PII) and correlation IDs.
3. Update `docs/governance/SLOs-SLIs.md` if SLI changes or new metric surfaces.

### 5.5 Security‑sensitive change

1. Validate authn/authz behavior and tenant isolation.
2. Update SECURITY notes and add tests covering misuse/abuse paths.

---

## 6) PR Quality Gate (must pass)

- ✅ **Build/tests:** `gradle test` green.
- ✅ **pre-commit:** all hooks pass (YAML/MD, Prettier, Google Java Format, commitlint).
- ✅ **Docs/Contracts:** updated when API surface changed.
- ✅ **Security:** tenant isolation, headers, idempotency validated.
- ✅ **No secrets** in diffs; structured logs only.

**PR Title/Commit message:** Conventional Commits, e.g. `feat(account): add bank attribution to transactions`.

**PR Description** (agents should include):

```md
## Summary

What changed and why.

## Checklist

- [ ] Docs updated (ADRs/Scope/Contracts)
- [ ] Tests updated (unit/integration/CDC)
- [ ] Security reviewed (authz, tenant scoping, headers)
- [ ] Migration safe (Flyway forward/backward tested)
```

---

## 7) File & Directory Policy

- **Allowed to edit:** `src/**`, `docs/**`, `.github/**`, `.pre-commit-config.yaml`, `.commitlintrc.cjs`, `scripts/**`, `build.gradle[.kts]`, `settings.gradle[.kts]`.
- **Do not add:** Gradle wrapper to VCS (CI uses `setup-gradle`).
- **.gitignore:** may include cache dirs (e.g., `.git-hooks-cache/`).
- **MkDocs:** do not remove Mermaid support; `mkdocs.yml` remains excluded from YAML hook.

---

## 8) Conventions

- **Branches:** `feat/*`, `fix/*`, `chore/*`, `docs/*`, `refactor/*`.
- **Commits:** Conventional Commits; subject should avoid UPPERCASE acronyms unless the rule is relaxed.
- **Labels:** `area/*`, `module/*`, `type/*`, `prio/*` when creating issues/PRs.

---

## 9) Commands Agents May Use

```bash
# Build & test
gradle -q test

# Repo hygiene
pre-commit run --all-files

# Formatter cache/script
chmod +x scripts/google-java-format.sh || true

# MkDocs (local via Docker, optional in later iterations)
docker compose up docs
```

---

## 10) Safety & Compliance

- No PII or secrets in code, configs, logs, commits, or PRs.
- Redact sensitive fields in logs; use correlation IDs.
- Respect tenant boundaries at all layers; treat cross‑tenant attempts as **security events**.

---

## 11) Escalation & Human‑in‑the‑loop

- If uncertain about requirements or encountering ambiguity, open a **draft PR** or issue labeled `needs/decision` and request human review.
- For potential breaking changes, create/update an **ADR** and reference it in the PR.

---

## 12) Change Control for AGENTS.md

- Update this document when roles, guardrails, or workflows change.
- Record rationale in `docs/governance/scope-CHANGELOG.md` and/or a new ADR.
