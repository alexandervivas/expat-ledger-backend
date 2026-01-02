# ADR-007: Hosting on Render Free Tier

## Status

Accepted — 2025-09-29

## Context

We need a zero-cost public environment for demos. Render Free offers Web Services (Docker/Node) and managed Postgres. Constraints:

- Postgres Free: limited (~1 GB) and **expires ~30 days** → not for production.
- Web Services (Free): can **idle/suspend**, leading to **cold starts** after inactivity.
- RabbitMQ hosting deferred; keep broker local/Testcontainers until Iteration 3+.

## Decision

- Deploy **API (Scala 3)** as a Render **Web Service (Docker)**.
- Deploy **Frontend (Next.js)** as a Render **Web Service (Node SSR)**.
- Use **Render Postgres (Free)** for demos only; add seeding & export scripts.
- Track two latency views: steady-state P95 (SLO) and first-hit-after-idle P95 (informational).

## Consequences

- Data loss risk when DB expires → nightly export + quick re-seed.
- Cold starts → acceptable for demos; add dashboard panel for first-hit latency.
- SLOs explicitly exclude idle cold-start latencies.

## Actions

- Add `render.yaml` at repo root.
- Add `docs/ops/deployment-render.md` with setup steps.
- Update `docs/governance/SLOs-SLIs.md` and Scope CHANGELOG.
