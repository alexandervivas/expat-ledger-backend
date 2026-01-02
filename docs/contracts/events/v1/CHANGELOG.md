# Events v1 — CHANGELOG

## 2025-10-15

- Introduce `tenant.created.v1` with fields: eventId, occurredAt, tenantId, name, ownerId. Reason: enable event-sourced projections and cross-service integration when a tenant is created.
- Introduce `user.created.v1` with fields: eventId, occurredAt, userId, name, email. Reason: enable event-sourced projections and cross-service integration when a user is created.

## 2025-09-29

- Introduce `transaction.imported.v1` with fields: tenantId, accountId, transactionId, bankId, sourceId, bookingDate, amount, currency.
- Introduce `account.updated.v1` with minimal snapshot for projections.
