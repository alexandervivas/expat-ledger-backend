# ADR-012: Hexagonal Layers and DDD Enforcement

## Status

Accepted — 2026-01-03

## Context

We need to ensure strict adherence to Hexagonal Architecture and Domain-Driven Design (DDD) to maintain a clean separation of concerns and prevent infrastructure details from leaking into the domain and application layers.

## Decision

1.  **Layer Separation**:

    - **Domain**: Contains entities, value objects, domain events, and repository ports (interfaces). It must have zero dependencies on other layers or infrastructure frameworks (except for basic functional libraries like Cats).
    - **Application**: Contains use cases (Application Services), input/output models (DTOs), and ports for cross-cutting concerns like `UnitOfWork` (transaction management).
    - **Infrastructure**: Contains adapters (implementations of ports), such as database repositories (Skunk), external service clients, gRPC/REST controllers, and configuration.

2.  **Transaction Management**:

    - Use a `UnitOfWork` port in the Application layer to drive transactions.
    - The `UnitOfWork.atomic` method must not leak infrastructure-specific types (e.g., `skunk.Session`) to the Application layer.

3.  **One Entity Per File**:

    - Every domain entity, value object, and event must reside in its own file to improve discoverability and maintainability.

4.  **Package Naming**:
    - Standardize package naming to reflect these layers: `com.expatledger.<module>.domain`, `com.expatledger.<module>.application`, `com.expatledger.<module>.infrastructure` (or specific names like `persistence`, `api`).

## Consequences

- Increased boilerplate due to port/adapter separation.
- Better testability: Domain and Application layers can be tested with pure mocks/stubs without infrastructure.
- Clearer boundaries and easier navigation of the codebase.
- Future infrastructure changes (e.g., switching from Skunk to another DB library) will be isolated to the infrastructure layer.
