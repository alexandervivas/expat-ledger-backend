# ADR-002: FX Provider SPI

## Status

Accepted — 2025-09-29

## Context

The system must calculate historical conversions between USD, EUR, and COP at the transaction date, with the flexibility to change sources.

## Decision

- Define a **pluggable SPI** for historical FX (per day, per currency pair).
- Implement a local **cache** keyed by (date, from, to).
- Initial provider: stub for tests; real provider chosen via a follow-up spike.

## Consequences

- Swap FX providers without core changes.
- Deterministic tests with stubbed rates.
