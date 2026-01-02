# Scope CHANGELOG

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
