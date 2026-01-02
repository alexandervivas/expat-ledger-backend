# ADR-010: Use sbt as the Build Tool

## Status

Accepted

## Context

The project has pivoted to a pure Scala 3 native stack. While Gradle with the Scala plugin was initially considered, sbt (Scala Build Tool) is the de facto standard in the Scala ecosystem. It provides better integration with Scala-specific tools (like Scalafmt, Wartremover, and various compiler plugins) and has superior support for incremental compilation through Zinc. The user expressed a preference for sbt over Gradle for building Scala applications.

## Decision

We will use **sbt** as the primary build tool for The Expat Ledger.

- Discard **Gradle** and the Gradle wrapper.
- Implement a multi-module sbt project structure.
- Use `sbt-scalafmt` for code formatting.
- Use `sbt-tpolecat` or similar for recommended scalac options.

## Consequences

- **Positive**: Better alignment with the Scala ecosystem, better IDE support (Metals/IntelliJ), more idiomatic build definitions for Scala developers, and access to a wide range of sbt-specific plugins.
- **Negative**: Requires transitioning existing Gradle configurations (if any) to sbt.
- **Risk**: Learning curve for those only familiar with Gradle/Maven, though sbt's syntax has improved significantly in recent versions.

## Actions

- Create `build.sbt` at the project root.
- Create `project/build.properties` and `project/plugins.sbt`.
- Update all documentation and CI workflows to use sbt commands.
