# ADR-015: Manual Dependency Injection with Resource-safe Modules

## Status

Accepted

## Context

Previous attempts to use dependency injection frameworks like Google Guice (ADR-014) and Scala 3 Contextual Abstractions (ADR-013) proved problematic when integrated with `cats.effect.Resource`. Guice, in particular, struggled to manage the acquisition and release of non-blocking resources like Skunk session pools, leading to potential connection leaks and complex runtime wiring issues.

We need a dependency injection strategy that:

1. Is 100% compile-time safe.
2. Respects functional purity and `cats-effect` resource management.
3. Provides maximum transparency in component wiring.

## Decision

We will adopt **Manual Dependency Injection** orchestrated through a "Resource Tree" in the application entry point.

1.  **Discard Guice**: Remove all dependencies on Google Guice and `jakarta.inject`.
2.  **Constructor Injection**: Components will use plain Scala classes with constructor injection.
3.  **Explicit Wiring**: All dependencies will be wired in `Main.scala` (or module-specific entry points) using Resource for-comprehensions or plain instantiation within a resource scope.
4.  **Module Pattern**: For larger services, group related components into modules (plain classes or traits) to mitigate "prop-drilling".

## Consequences

### Positive

- **Compile-time Safety**: No more runtime "Missing Binding" or reflection-related errors.
- **Resource Integrity**: Guaranteed cleanup of resources (DB pools, HTTP clients) as they are tied to the application's lifecycle via `Resource`.
- **Transparency**: The dependency graph is explicitly visible in the code.
- **No Magic**: Reduces the learning curve for developers unfamiliar with specific DI frameworks.

### Negative

- **Boilerplate**: Requires manual wiring of components, which can grow as the project scales.
- **Verbosity**: `Main.scala` might become large if not properly modularized.

## Alternatives Considered

- **Jam / MacWire**: Compile-time DI macros. These were considered but rejected to keep the build process simple and avoid additional dependencies until manual wiring becomes truly unmanageable.
- **ReaderT / Kleisli**: Functional DI patterns. Rejected as they introduce significant cognitive overhead and boilerplate without substantial benefits for the current project scope.
