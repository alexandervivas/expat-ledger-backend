# ADR-004: Idempotency Strategy

## Status

Accepted — 2025-09-29

## Context

Clients and ingestion may retry writes; we must prevent duplicates.

## Decision

- Require `Idempotency-Key` header on mutating HTTP requests.
- Persist processed keys per route `(route, key)` with response hash.
- Transactions ingestion idempotency by `(tenant_id, source_id)`.

## Consequences

- Safe retries; observability of deduplicated requests.
- Minimal storage overhead for keys table.
