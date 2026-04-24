# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

A tennis tournament management and player ranking system for Hungarian tennis clubs. Core features: tournament lifecycle management, KTR (Hungarian Tennis Ranking) calculation, match tracking, player registration, and invoicing via Billingo.

## Build & Run

```bash
# Build
mvn clean package

# Production build (optimized frontend)
mvn clean package -Pproduction

# Run tests
mvn test

# Run (after build)
java --enable-preview -jar target/app.jar
```

Required environment variables:
```
CLEARDB_DATABASE_URL    # MySQL: mysql://user:pass@host:port/db
ENVIRONMENT             # DEV or PROD
PORT                    # default 7070
SENDGRID_PASSWORD
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
OAUTH_CALLBACK_URL
BILLINGO_API_KEY
```

## Architecture

Hexagonal architecture with manual DI wired in `ApplicationContext`:

```
infrastructure/ApplicationContext  ← single static wiring of all services
        ↓
domain/{ktr,match,tournament,player,user,email,invoice}  ← pure business logic
        ↓
infrastructure/database/*JdbcRepository  ← JDBI SQL
infrastructure/web/api/HttpServer        ← Javalin REST
infrastructure/ui/                       ← Vaadin Flow UI
```

**Entry point:** `Main.java` — reads env vars, builds `ApplicationContext`, starts HTTP server.

**UI:** Vaadin Flow 23 (server-side Java → HTML). No separate frontend SPA; Vite bundles Vaadin's generated TS. UI code lives in `infrastructure/ui/`.

**REST API:** Javalin served alongside Vaadin via embedded Jetty (`infrastructure/web/api/`).

## Domain Layer Key Concepts

- **KTR** (`domain/ktr/`) — The ranking algorithm. `KTRCalculator` applies time decay to historical matches; `KTRMath` converts game scores to points. This is the most algorithmically complex part of the codebase.
- **Match** (`domain/match/`) — `MatchService` + `MatchInfo` + `Head2HeadData`. Matches belong to tournaments; KTR is recalculated after each match result update.
- **Tournament** (`domain/tournament/`) — Has phases: registration → draw → in-progress → completed. `TournamentBoard` models the bracket.
- **Player registration** (`domain/player/registration/`) — A separate workflow from user accounts; players are registered to tournaments via `TENISZ_TOURNAMENT_REGISTRATION`.
- **Organization** enum — `KVTK` and `DEBRECEN` represent the two clubs; some logic branches on this.

## Database

MySQL in production, H2 in-memory for tests. Schema in `database/create-database.sql`. All tables prefixed `TENISZ_`. Access via JDBI in `infrastructure/database/`.

## Tests

- Unit tests: `src/test/java/hu/kits/tennis/domain/` — focus on KTR math and tournament logic
- Integration tests: use `InMemoryDataSourceFactory` (H2) and `SpyEmailSender`
- End-to-end: `test/test-cases/*.testcase` files — Markdown-style HTTP request/response specs
- JaCoCo coverage excludes `infrastructure/ui/**`

Run a single test class:
```bash
mvn test -Dtest=KTRCalculatorTest
```
