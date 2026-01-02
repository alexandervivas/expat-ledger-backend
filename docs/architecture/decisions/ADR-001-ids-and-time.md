# ADR-001: IDs & Time

## Status

Accepted — 2025-09-29

## Context

Consistency across modules and FX calculations requires unambiguous IDs and timestamps.

## Decision

- Use **UUID v4** as primary keys.
- Store all timestamps as **UTC** (`timestamptz`).
- APIs accept/return **ISO-8601** in UTC.

## Consequences

- Safe merges across tenants and imports.
- Deterministic historical FX conversion.
