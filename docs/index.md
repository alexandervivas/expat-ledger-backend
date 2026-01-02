# Personal Banking — Developer Docs

Welcome! This site documents the architecture, contracts, and governance for the Personal Banking project.

## Quick links

- **Architecture**:
  - [C4 Context](architecture/c4-context.md)
  - [C4 Containers](architecture/c4-container.md)
- **Decisions**:
  - [ADR Index](architecture/decisions/index.md)
- **Contracts**:
  - [OpenAPI v1 Changelog](contracts/openapi/v1/CHANGELOG.md)
- **Governance**:
  - [Scope CHANGELOG](governance/scope-CHANGELOG.md)
  - [Product CHANGELOG](governance/product-CHANGELOG.md)

## How to run docs locally

```bash
pip install mkdocs mkdocs-material
mkdocs serve
# open http://127.0.0.1:8000
```

## Contributing

- Use Conventional Commits.
- For breaking API/event changes: open an ADR and update contract changelogs.
- Keep diagrams and examples minimal and testable.
