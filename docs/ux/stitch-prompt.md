# Stitch Prompt — Onboarding & Core UI

Use this prompt in Stitch to generate the initial screens and components. Keep the inline bank creation pattern (tenant-scoped) and accessibility requirements.

---

You are a senior product designer generating responsive, accessible web app UIs.

Context

- Product: Multi-tenant Personal Banking web app (Next.js frontend, Scala 3 backend).
- Modules: tenant, users (RBAC OWNER/ADMIN/USER), accounts (CHECKING/SAVINGS), transactions (Excel ingestion), FX (USD/EUR/COP; historical conversions).
- Key constraints: WCAG 2.1 AA, JSON-friendly UI copy (i18n-ready EN/ES), **no separate CRUD page for banks**; bank creation must be inline from the dropdown and is tenant-scoped.

Global visual language

- Clean, minimal, modern; 8-pt grid; rounded-xl corners; subtle shadows; clear hierarchy.
- Typography: Inter (or similar), 14–16 base; 20–28 section headers; 12 captions.
- Light & Dark themes.
- Components: TopNav, Sidebar, Breadcrumbs, Button, Input, TextArea, Combobox (typeahead + “create new”), Select, DatePicker, Tabs, Card, Table (sticky header + pagination), File Dropzone, Progress, Toast, Modal, Chart container.

Screens (desktop + mobile variants)

1. Marketing landing with primary CTA “Sign up”.
2. Auth:
   - Option A (recommended): Auth0 Universal Login theme sheet.
   - Option B: Custom sign-up page (email/password or magic link).
3. Tenant setup: Tenant name, Default locale, Base currency.
4. Onboarding wizard:
   - Create accounts: Account name, Type (CHECKING/SAVINGS), Currency (USD/EUR/COP), Initial balance.
   - **Bank selection**: Combobox lists existing banks (tenant-scoped) + option “Add ‘{input}’ as a new bank” → confirm modal → create and select; microcopy explains tenant scoping.
   - Step summary before finish.
5. Dashboard:
   - Cards per account: native balance + EUR & COP equivalents (historical FX; show “as of <date>”).
   - Trends and category breakdown; placeholders for “Anomalies” and “30/60/90-day forecast”.
6. Transactions import:
   - Upload Excel → header mapping UI → progress → results (inserted/ignored/failed) → error download.
7. Account detail:
   - Running balance and transactions; filters; native + EUR/COP; tooltip with FX rate + source timestamp.
8. States: loading skeletons, empty states with CTA, error banners with retry.

Accessibility & i18n

- Visible focus; correct tab order; ARIA roles for combobox, progress, and dialog.
- Provide copy keys in EN and ES.

Interactions

- Combobox create-new: type → “Add ‘{text}’ as bank” → confirm → toast “Bank created for this tenant” → selection applied.

Handoff

- Provide component names/props near each screen.
- Provide Design Tokens for colors/spacing/radii/shadows/typography.
- Name frames: 01_Landing, 02_Auth_UniversalLogin, 03_TenantSetup, 04_Onboarding_Accounts, 05_Dashboard, 06_TransactionsImport, 07_AccountDetail, 08_States.

---
