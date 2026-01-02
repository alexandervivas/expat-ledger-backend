# OpenAPI v1 — CHANGELOG

## 2026-01-02

- Align schemas with ADR-001 (IDs & Time):
  - Add `format: uuid` to all ID fields (`tenantId`, `accountId`, `bankId`, `transactionId`).
  - Standardize string formats for UUIDs and ISO-8601 timestamps.

## 2025-10-01

- Establish baseline `/v1` CRUD stubs for tenants, accounts, banks, and transactions.
- Define shared RFC7807 Problem Details response and Idempotency-Key header for mutations.

## 2025-09-29

- Add `bankId` to `Transaction` schema.
- Add `/v1/banks` CRUD resources.
