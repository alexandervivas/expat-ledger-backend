# ADR-005: Bank Attribution & Multi-currency Balances

## Status

Accepted — 2025-09-29

## Context

We must identify the originating **bank** for each transaction and expose balances in **account currency + EUR + COP** using **historical FX**.

## Decision

- Introduce `bank` entity (tenant-scoped) and add `bank_id` FK to `transaction`.
- Compute balances on **read** using historical FX at transaction date; cache daily rates.
- Provide **Balance API** returning native + EUR + COP equivalents.

## Consequences

- One additional FK and supporting indexes: `(tenant_id, bank_id, booking_date)`.
- Slightly higher read cost; mitigated with FX/day cache and SQL view/projection.
- Leaves room for future balance snapshots if necessary; API remains stable.
