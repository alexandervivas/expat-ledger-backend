# Scala 3 Framework Recommendations for The Expat Ledger

To meet the requirements of a cross-border wealth management system using a distributed modular monolith architecture, the following Scala 3 frameworks and libraries are recommended. We have moved away from Spring Boot to avoid interoperability issues and embrace a pure Scala-native stack.

## 1. Web & API (Internal Sync)

- **Tapir**: Declarative, type-safe API endpoints. It can generate OpenAPI documentation and has excellent Scala 3 support.
- **Http4s**: A minimal, idiomatic Scala interface for HTTP services. It is purely functional and works seamlessly with Cats Effect.
- **ZIO Http**: A high-performance, type-safe HTTP library for ZIO.

## 2. Distributed Communication (gRPC & Events)

- **fs2-grpc**: Purely functional gRPC for Scala, built on top of FS2 and Cats Effect.
- **CloudEvents Scala SDK**: For standardizing asynchronous communication over RabbitMQ.
- **fs2-rabbit**: A functional stream-based client for RabbitMQ, fitting perfectly with the Scala 3 ecosystem.

## 3. Persistence & Migration

- **Doobie**: A purely functional JDBC layer for Scala. Excellent for PostgreSQL integration.
- **Skunk**: A purely functional PostgreSQL library for Scala. It doesn't use JDBC and is optimized for Postgres.
- **Quill**: Compile-time type-safe queries. Works great with Scala 3.
- **Flyway**: Continue using Flyway for schema migrations as it is language-agnostic.

## 4. Domain Modeling & Logic

- **Cats / Cats Effect**: The standard for functional programming in Scala. Essential for managing side effects and building robust domain logic.
- **ZIO**: An alternative ecosystem for asynchronous and concurrent programming, offering a powerful effect system and environment-based dependency injection.
- **Iron / Refined**: For type-level validation (e.g., ensuring a `Money` amount is positive).

## 5. Testing

- **MUnit / Weaver-test**: Modern, lightweight testing frameworks for Scala 3.
- **Testcontainers-scala**: Scala wrapper for Testcontainers, essential for Postgres and RabbitMQ integration tests.

## Summary Table

| Category          | Recommended Tool            | Scala 3 Status | Rationale                           |
| :---------------- | :-------------------------- | :------------- | :---------------------------------- |
| **Language**      | Scala 3.x                   | Native         | Primary development language.       |
| **HTTP Server**   | Http4s / Tapir              | Native         | Purely functional, type-safe APIs.  |
| **Sync Comms**    | gRPC (fs2-grpc)             | Native         | High performance, contract-first.   |
| **Async Comms**   | RabbitMQ (fs2-rabbit)       | Native         | Distributed messaging, CloudEvents. |
| **Database**      | PostgreSQL (Doobie / Skunk) | Native         | Purely functional DB access.        |
| **Validation**    | Iron                        | Native         | Type-level constraints.             |
| **JSON**          | Circe                       | Native         | Industry standard for Scala.        |
| **Effect System** | Cats Effect / ZIO           | Native         | Robust management of side effects.  |
