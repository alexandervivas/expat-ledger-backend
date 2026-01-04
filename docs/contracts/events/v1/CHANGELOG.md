# Events v1 — CHANGELOG

## 2026-01-04

- Externalize Avro schemas: Implement `AvroSchemaLoader` in `shared-kernel` to pull schemas directly from `.avsc` files instead of hardcoding them in code.

## 2026-01-02

- Refactor internal event model in `shared-kernel` to improve type safety:
  - Introduce `Event` trait with common fields: `id`, `aggregateId`, `occurredAt`.
  - Redefine `DomainEvent` as a trait extending `Event`.
  - Redefine `OutboxEvent` as a concrete case class extending `Event`.
- Standardize namespaces to `com.expatledger.events.v1` for all event schemas.
- Align `AccountUpdated` and `TransactionImported` with ADR-008 (CloudEvents):
  - Add `eventId` and `occurredAt` (ISO-8601 instant) fields.
  - Standardize metadata documentation.
- Rename `updatedAt` to `occurredAt` in `AccountUpdated` for cross-event consistency.

## 2025-10-15

- Introduce `tenant.created.v1` with fields: eventId, occurredAt, tenantId, name. Reason: enable event-sourced projections and cross-service integration when a tenant is created.
- Introduce `user.created.v1` with fields: eventId, occurredAt, userId, name, email. Reason: enable event-sourced projections and cross-service integration when a user is created.

## 2025-09-29

- Introduce `transaction.imported.v1` with fields: tenantId, accountId, transactionId, bankId, sourceId, bookingDate, amount, currency.
- Introduce `account.updated.v1` with minimal snapshot for projections.
