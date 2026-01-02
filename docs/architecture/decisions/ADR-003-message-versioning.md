# ADR-003: Message Versioning

## Status

Accepted — 2025-09-29

## Context

Events evolve; consumers need compatibility.

## Decision

- Topic naming includes version, e.g., `transaction.imported.v1`.
- **Additive** changes only in-place; breaking changes → new topic `.v2`.
- Maintain schema registry and CDC tests.

## Consequences

- Consumers can migrate at their own pace.
- Clear upgrade path for breaking changes.
