# Grand Line Stonks

A One Piece–flavored prediction market. Users sign up, receive 10,000 play-money "berries", and bet them on the outcomes of pirate-world questions; prices are quoted by an LMSR automated market maker and read as probabilities. Every berry movement flows through an append-only double-entry ledger, market lifecycles are enforced by an explicit state machine, and all money paths (bets, settlement, refunds) are idempotent and proven safe under concurrent load by tests that run against real PostgreSQL.

## Tech stack

- **Backend** — Java 21, Spring Boot 3.5, Spring Security (JWT), JPA/Hibernate, Flyway, PostgreSQL 17
- **Frontend** — Next.js 14 (React 18, TypeScript), Tailwind CSS, React Hook Form + Zod, Recharts
- **Infra & testing** — Docker Compose, Testcontainers, Micrometer metrics, springdoc OpenAPI

## Structure

```
├── docker-compose.yml          # postgres + backend + frontend
├── backend/
│   └── src/
│       ├── main/java/com/grandlinestonks/
│       │   ├── auth/           # signup, login, JWT
│       │   ├── account/        # balance-holding accounts
│       │   ├── ledger/         # double-entry core (source of truth)
│       │   ├── transfer/       # user-to-user transfers
│       │   ├── market/         # markets, LMSR, betting, settlement
│       │   ├── portfolio/      # positions & transaction history
│       │   ├── seed/           # demo data (opt-in)
│       │   ├── config/         # security, request-id logging
│       │   └── error/          # domain exceptions → HTTP mapping
│       ├── main/resources/db/migration/   # Flyway schema (V1–V4)
│       └── test/               # unit + Testcontainers integration tests
└── frontend/
    └── src/
        ├── app/                # pages: /login, /signup, /markets, /markets/[id], /portfolio
        ├── components/         # Nav, BetForm, PriceChart, ProbabilityBar, AuthForm
        └── lib/                # api client, auth context, types, palette
```

## Running it

**Everything in Docker** (needs Docker Desktop):

```bash
docker compose up --build
```

Then open http://localhost:3000 (app) — seeded login: `zoro_bets` / `grandline123` — and http://localhost:8080/swagger-ui.html (API docs).

**For development:**

```bash
docker compose up -d postgres              # database only
cd backend && ./mvnw spring-boot:run       # API on :8080 (Java 21+)
cd frontend && npm install && npm run dev  # UI on :3000 (Node 20+)
```

**Tests** (Docker must be running — integration tests use Testcontainers):

```bash
cd backend && ./mvnw test
```
