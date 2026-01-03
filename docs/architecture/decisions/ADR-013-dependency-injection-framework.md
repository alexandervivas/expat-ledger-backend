# ADR-013: Dependency Injection with Scala 3 Contextual Abstractions

## Status

Superseded by ADR-014

## Context

With the separation of service contracts (traits) and implementations, we need a way to wire dependencies. While external DI frameworks like Distage or MacWire are available, we want to maximize the use of Scala 3 native features and minimize external dependencies that might complicate the build or environment.

## Decision

Adopt **Scala 3 Contextual Abstractions** (`given`, `using`) as the primary Dependency Injection (DI) framework for the project.

1.  **Contextual Wiring**: Services and adapters take their dependencies as `using` parameters.
2.  **DI Container**: Create a `Container` class (e.g., `TenantContainer`) that holds `given` instances for all components in a module.
3.  **Lifecycle Management**: Use Cats Effect `Resource` to manage the creation and destruction of the container and its underlying infrastructure (e.g., database sessions).
4.  **No Reflection**: All wiring is checked at compile-time by the Scala 3 compiler.

## Consequences

-   **Native Experience**: No extra libraries are required for basic DI, adhering to the project's goal of using Scala-native features.
-   **Type Safety**: Errors in wiring are caught at compile-time.
-   **Testability**: Test-specific dependencies can easily be provided using local `given` instances or by passing them explicitly to constructors.
-   **Limited Auto-discovery**: Unlike some reflection-based frameworks, we must explicitly define the `given` instances in a container or module, but this improves clarity and discoverability in Hexagonal Architecture.
-   **Compatibility**: This approach is 100% compatible with the current build environment and avoids issues with sbt dependency resolution.
