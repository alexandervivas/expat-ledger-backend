# The Expat Ledger

[![api-ci](https://github.com/alexandervivas/personal-banking-be-java/actions/workflows/ci.yml/badge.svg)](https://github.com/alexandervivas/personal-banking-be-java/actions/workflows/ci.yml)

Cross-border wealth management system for expatriates managing "dual financial lives" (USD, EUR, COP). **Scala 3**, **Cats Effect**, **Http4s**, **sbt**, **PostgreSQL**. 

## Context & Scope (Cross-Border Focus)

A system born from the immigrant experience to manage accounts across home and host countries.

- **Tech Stack**: Backend: Scala 3 (using Scala-native features), Cats Effect, Http4s.
- **Architecture**: Distributed Modular Monolith. Each domain is an independent service.
- **Communication**: gRPC (Internal Sync), CloudEvents 1.0 over RabbitMQ (Internal Async).
- **Discovery**: Decentralized or Scala-native discovery (e.g., via Consul or static config for now).
- **Domain Core**: Multi-base currency engine, Remittance linkage, and dual tax-year reporting.

> Docs-as-code live under `docs/` (ADRs, C4, OpenAPI, governance). Deployment notes: `docs/ops/deployment-render.md`.

---

## Table of Contents

- [Development Environment](#development-environment)
- [First‑time Setup](#first-time-setup)
- [Day‑to‑day Commands](#day-to-day-commands)
- [Conventional Commits](#conventional-commits)
- [Pull Request Template](#pull-request-template)
- [Continuous Integration](#continuous-integration)
- [Editor/IDE Tips](#editoride-tips)
- [Troubleshooting](#troubleshooting)

---

## Development Environment

This repository contains the **API** only. Tooling and docs below reflect that.

**Prerequisites**

- **Scala 3.x**
- **sbt** (Scala Build Tool)
- **Python + pipx** (for `pre-commit`)
- **Node.js ≥ 18** (for local Conventional Commits validation via `commitlint`)
- (Optional) **Docker** for local infra (used in later iterations)

> **Why Node?** We run **commitlint** via a pre-commit Node hook (`language: node`) with pinned dependencies; Node ≥ 18 is required.

### Tooling specifics

- **pre-commit config file**: `.pre-commit-config.yaml` (repo root).
- **Prettier (Node hook)**: formats `*.json`, `*.md`, `*.yaml`/`*.yml` via a local Node hook pinned to `prettier@3.3.3`.
- **Scalafmt**: Enforced via sbt or pre-commit.
- **MkDocs YAML**: `mkdocs.yml` uses a Python constructor tag for Mermaid and is intentionally **excluded** from the `check-yaml` hook to avoid false positives.

---

## First‑time Setup

Install the git hooks (runs once per machine):

```bash
# Install pre-commit (recommended via pipx)
pipx install pre-commit  # or: pip install --user pre-commit

# Ensure formatter script is executable (one time)
chmod +x scripts/*.sh || true

# Install local git hooks (pre-commit + commit-msg)
make pre-commit-install
```

> If you change hook definitions, re-run: `pre-commit clean && pre-commit install`.

---

## Day‑to‑day Commands

The repo ships a minimal **Makefile** that wraps sbt and common hygiene tasks.

```bash
make build     # sbt compile
make test      # sbt test
make check     # sbt scalafmtCheck
make lint      # Run all pre-commit hooks across the repo
make format    # Apply formatting hooks (JSON/Markdown/Scala) via pre-commit
```

> Prefer **sbt** in CI and locally.

---

## Conventional Commits

All commit messages **must** follow **Conventional Commits**. Examples:

- `feat(account): enforce currency validation (USD/EUR/COP)`
- `fix(transaction): handle duplicate sourceId on ingestion`
- `chore(ci): enable sbt cache`

The local `commit-msg` hook validates messages. If it fails, amend:

```bash
git commit --amend
```

> **Note:** The default `subject-case` rule disallows UPPERCASE acronyms in the subject (e.g., `GJF`). Use lower-case (`gjf`) or avoid using acronyms altogether.

---

## Pull Request Template

Every PR should include a short summary and tick the relevant checklist items:

- Scope/ADRs updated when applicable
- **OpenAPI** + contract **CHANGELOG** updated if the API surface changed
- Tests updated (unit / integration / CDC)
- Security considerations reviewed (headers, authz, tenant scoping)

> The template lives at `.github/PULL_REQUEST_TEMPLATE.md` and appears automatically when opening a PR.

---

## Continuous Integration

Minimal GitHub Actions workflow (`.github/workflows/ci.yml`) runs on push/PR:

- Java 21 setup
- `sbt test`
- `pre-commit run --all-files`

Keep builds green; fix style/format issues locally with `make format`.

---

## Editor/IDE Tips

- Enable **“format on save”** if your IDE supports Google Java Format.
- Ensure the IDE uses **Java 21** and the **sbt** build.
- Consider installing a **Conventional Commits** plugin/extension.

---

## Local Infra (Docker Compose)

This project publishes domain events to RabbitMQ. Two Compose files are provided:

- docker-compose.yml → infra/dev utilities (RabbitMQ, Docs)
- docker-compose.app.yml → full application (Eureka from this repo, Tenants, API, RabbitMQ)

Start full application stack (builds images from source, ensures Eureka is healthy before others start):

```bash
docker compose -f docker-compose.app.yml up --build
```

Start only infra utilities (no app containers):

```bash
docker compose up -d rabbitmq docs
```

Services (full app stack):

- Eureka (service discovery from modules/eureka-server) at http://localhost:8761
- Tenants service: HTTP http://localhost:8081, gRPC :9091
- API gateway: HTTP http://localhost:8080
- RabbitMQ (ports 5672, 15672). Management UI: http://localhost:15672 (default: guest/guest)

Environment variables consumed by services (with defaults):

- RABBITMQ_HOST=localhost (in containers we set rabbitmq; external clients use localhost)
- RABBITMQ_PORT=5672
- RABBITMQ_USERNAME=guest
- RABBITMQ_PASSWORD=guest
- RABBITMQ_VHOST=/
- EUREKA_URL=http://localhost:8761/eureka/
- EUREKA_REGISTER=true|false (api=false by default, tenants=true)
- EUREKA_FETCH=true|false (default: true)
- EVENTS_EXCHANGE_PREFIX=events.v1.
- EVENTS_ROUTING_KEY=events
- TENANTS_HTTP_PORT=8081, TENANTS_GRPC_PORT=9091
- API_HTTP_PORT=8080

Notes:

- We use the in-repo eureka-server module instead of a third-party image. Health checks gate dependent services.
- H2 is embedded; no DB container is required.

---

## Messaging Model (Event Sourcing)

- We publish to RabbitMQ exchanges by event type (topic exchanges), not dedicated queues.
- Exchange naming: `${EVENTS_EXCHANGE_PREFIX}${eventName}`. Example: `events.v1.tenant.created.v1`.
- Default routing key: `${EVENTS_ROUTING_KEY}` (defaults to `events`).
- Consumers bind their own queues to the exchanges they care about, enabling event-sourced projections.

### Avro serialization

- Events are serialized as Avro GenericRecord objects using contracts under `docs/contracts/events/v1`.
- Current events:
  - tenant.created.v1 → schema fields: eventId, occurredAt, tenantId, name, ownerId
  - user.created.v1 → schema fields: eventId, occurredAt, userId, name, email
- The tenants service ships the same `.avsc` files in its classpath (`modules/tenants/src/main/resources/contracts/events/v1/`) to ensure runtime availability.
- See `docs/contracts/events/v1/CHANGELOG.md` for versioning decisions and changes (see also ADR-003).

### CloudEvents compliance

We comply with CloudEvents 1.0 (binary content mode) when publishing to RabbitMQ. The Avro payload is the CloudEvents `data`; CloudEvents attributes are mapped to AMQP headers with the `ce_` prefix:

- ce_specversion = 1.0
- ce_type = event name (e.g., tenant.created.v1)
- ce_source = tenants-service
- ce_id = UUID (matches Avro field eventId)
- ce_time = RFC3339 timestamp (matches Avro field occurredAt)
- ce_subject = domain identifier (tenantId for tenant events, userId for user events)
- ce_datacontenttype = application/avro
- ce_dataschema = urn:avro:schema:<full.avro.namespace.Name> (e.g., urn:avro:schema:com.eureckah.banking.events.v1.TenantCreated)

Notes:

- This follows CloudEvents Core v1.0 and the binary-mode pattern; RabbitMQ uses AMQP 0.9.1 which lacks an official binding, but the header mapping is a recognized approach.
- Consumers can read the Avro schema from `ce_dataschema` and may also find the full Avro schema string in a convenience header `schema`.
- See ADR-008 for rationale and details.

---

## Troubleshooting

- **Hooks don’t run** → re‑install: `make pre-commit-install`.
- **commitlint fails** → ensure Node ≥ 18 (`node -v`) and re‑try.
- **sbt not found** → ensure `sbt` is installed and available in your PATH.
- **CI fails on pre-commit** → run `make lint`, commit the fixes, push again.
- **YAML constructor error in `mkdocs.yml`** → this file is excluded from the `check-yaml` hook because it uses `!!python/name:` for Mermaid. If you still see warnings, run `pre-commit clean && pre-commit install`.
- **Scala files not reformatted** → ensure `sbt scalafmtAll` runs correctly.
- **Commit rejected by subject-case** → Amend with a compliant subject, e.g.: `git commit --amend -m "chore(tooling): add pre-commit, prettier, commitlint, and gjf"` or avoid using acronyms altogether.

---

### Paths of interest

- Contracts (OpenAPI): `docs/contracts/openapi/v1/openapi.yaml`
- ADRs: `docs/architecture/decisions/`
- Governance: `docs/governance/`
- Deployment: `docs/ops/deployment-render.md`

---

> Status: _API‑first iterations_. Frontend lives in a separate repository and will be addressed in future sprints.
