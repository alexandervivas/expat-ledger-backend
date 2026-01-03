.PHONY: help build test check lint format pre-commit-install

help:
	@echo "Targets:"
	@echo "  lint                - pre-commit in the whole repo"
	@echo "  pre-commit-install  - instala hooks (incluye commit-msg)"

pre-commit-install:
	pre-commit install
	pre-commit install --hook-type commit-msg
	@echo "✔ Hooks instalados"
