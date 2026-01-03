# ADR-014: Standardizing Dependency Injection with Google Guice

## Status

Accepted — 2026-01-03

## Context

While ADR-013 introduced Scala 3 Contextual Abstractions for Dependency Injection (DI), the team has decided to standardize on a more traditional and widely recognized DI framework to improve discoverability and ease of use for developers coming from other ecosystems. Google Guice is a mature, lightweight DI framework that fits well with the project's modular structure.

## Decision

1.  **Adopt Google Guice**: Use Google Guice (version 7.0.0+) as the primary DI framework across all services.
2.  **Supersede ADR-013**: This decision replaces the use of Scala 3 `given`/`using` for component wiring.
3.  **Module-based Configuration**: Each service will define its own Guice `AbstractModule` (e.g., `TenantModule`) to configure bindings.
4.  **Constructor Injection**: Prefer constructor injection with the `@Inject` (from `jakarta.inject`) annotation for all components.
5.  **TypeLiteral for Higher-Kinded Types**: Use `TypeLiteral` when binding components with type parameters (e.g., `TenantService[IO]`).

## Consequences

- **Standardization**: Provides a familiar DI pattern for most JVM developers.
- **Explicit Wiring**: Dependencies are explicitly declared in modules, making the wiring logic easier to trace than implicit contextual abstractions.
- **Library Dependency**: Introduces `guice` and `jakarta.inject-api` as project dependencies.
- **Runtime vs Compile-time**: Guice performs wiring at runtime (during injector creation), whereas Scala 3 contextual abstractions are checked at compile-time. We accept this trade-off for the benefit of standardization and reduced complexity in service entry points.
