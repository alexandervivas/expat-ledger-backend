# ADR-009: Transition to Scala 3 and Native Frameworks

## Status

Accepted

## Context

The project was originally conceived as a Java 21 Spring Boot application. However, to leverage modern functional programming features, better type safety, and more concise syntax, the decision has been made to switch the primary development language to Scala 3. Furthermore, to avoid interoperability issues and complexity arising from mixing Scala with Spring Boot, we have decided to move away from Spring Boot in favor of a pure Scala-native ecosystem.

## Decision

We will use **Scala 3** for all service implementations.

- Discard **Spring Boot** as the base framework.
- Adopt a pure Scala-native stack (e.g., Cats Effect, Http4s, Tapir, Doobie/Skunk).
- All infrastructure (Service Discovery, Gateway, Messaging) will be implemented using Scala-native libraries or language-agnostic tools.

## Consequences

- **Positive**: More expressive and idiomatic code, better handling of domain logic through Scala's type system, improved developer productivity, and elimination of Scala-Java interoperability friction.
- **Negative**: Requires the team to be proficient in Scala 3 and its functional ecosystem. Loss of some Spring Boot "magic" and its massive pre-built integration ecosystem.
- **Risk**: Smaller community/resources for some specific integrations compared to Spring Boot. Potential learning curve for developers coming from a Java/Spring background.
