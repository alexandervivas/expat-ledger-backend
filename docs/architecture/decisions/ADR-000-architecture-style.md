# ADR-000: Architecture Style

## Status

Accepted — 2025-09-29

## Context

We need an evolvable architecture supporting incomplete specs, event notifications, and strict NFRs (performance, security, observability).

## Decision

Adopt a **Hexagonal architecture** within a **Modular Monolith**, with REST APIs (`/v1`) and **event notifications via RabbitMQ**. Use **OpenAPI** for contracts, **Avro/JSON Schema** for events. Observability via **OpenTelemetry → Prometheus + Grafana**.

## Consequences

- Clear separation of domain/application/infrastructure; adapters for DB, messaging, FX, Excel.
- Easier local development and later extraction of services if needed.
- Contract-first development with CDC feasible.
