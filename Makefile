.PHONY: help build test check lint format pre-commit-install

help:
	@echo "Targets:"
	@echo "  lint                - pre-commit in the whole repo"
	@echo "  pre-commit-install  - instala hooks (incluye commit-msg)"

lint:
	python -m pre_commit run --all-files

format:
	python -m pre_commit run --all-files

pre-commit-install:
	python -m pre_commit install
	python -m pre_commit install --hook-type commit-msg
	@echo "✔ Hooks instalados"
