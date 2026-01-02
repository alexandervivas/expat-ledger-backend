# ADR-011: Use ip4s for Host and Port Validation

## Status
Accepted

## Context
In a distributed system, hostnames and ports are critical configuration parameters. Relying on raw `String` for hosts and `Int` for ports can lead to runtime errors due to invalid values (e.g., negative port numbers, invalid IP formats). To improve type safety and validation, we need a standard way to represent these types across all modules.

## Decision
We will use **com.comcast.ip4s** (`Host` and `Port`) to represent and validate all host and port configurations throughout the codebase.

- **Host**: Use `com.comcast.ip4s.Host` for IP addresses and hostnames.
- **Port**: Use `com.comcast.ip4s.Port` for TCP/UDP ports.
- **Validation**: Configurations should be parsed into these types as early as possible (e.g., during service discovery or config loading).

## Consequences
- **Positive**:
    - Compile-time and early runtime validation of network configurations.
    - Improved type safety by avoiding "primitive obsession".
    - Seamless integration with the Http4s/Ember ecosystem, which already uses `ip4s`.
- **Negative**:
    - Slightly more verbose configuration parsing (need to handle `Option` or `Either` during parsing).
    - Adds a direct dependency on `ip4s-core` in the `shared-kernel`.
