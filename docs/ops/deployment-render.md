# Deployment — Render (Free Tier)

This environment is for demos only. Expect cold starts and DB expiry (~30 days) on the free plan.

## Blueprint

Render supports declarative deploys via `render.yaml` at the repository root.

### Steps

1. Commit `render.yaml` (see repo root).
2. In Render, create a **Blueprint** pointing to your GitHub repo.
3. Set env vars if needed (secrets in Render dashboard):
   - `SPRING_PROFILES_ACTIVE=prod`
   - `NEXT_PUBLIC_API_BASE_URL=https://<api-service>.onrender.com`
4. First deploy will provision the DB; the app applies Flyway migrations on boot.
5. Verify `/health` on the API service.

## Known constraints

- **Cold starts**: services may idle; first request latency spikes.
- **DB expiry**: Free Postgres may be deleted after ~30 days. Use seed/export scripts.

## Seed & export (outline)

- **Seed**: Java runner or SQL script to insert minimal tenants/accounts/banks.
- **Export**: `pg_dump` job invoked locally; do not dump secrets/PII.

## Next steps

- Add synthetic pings only during **scheduled demos** (optional), and document them.
