# Traçabilité IA

Monorepo for a "traceability of AI-assisted decisions" project (PFA 2025/2026). It contains three services plus a database:

- `backend/` — Spring Boot 3.4 / Java 21 REST API (Maven, wrapper `./mvnw`), talks to PostgreSQL.
- `frontend/` — Angular 21 SSR app (npm).
- `ml-service/` — Python/Flask machine-learning decision service (scikit-learn RandomForest, multi-domain).
- PostgreSQL 16 — used by the backend.

The canonical, project-authored way to run everything together is `docker-compose up` (see `README.md`). Per-service docs: `README.md`, `ml-service/README.md`, `ml-service/EXAMPLES.md`, `frontend/README.md`, `ARCHITECTURE.md`.

## Cursor Cloud specific instructions

Docker is not available in the Cloud VM, so services are run natively (not via `docker-compose`). Standard build/test/run commands live in `README.md`, `frontend/package.json` (scripts), `backend/pom.xml`, and `ml-service/README.md`; only the non-obvious caveats are captured here.

### Current implementation state (important context)
The repository is mostly scaffolding. The **ML service (`ml-service/app.py`) is the only fully implemented component** and is the app's core functionality (endpoints `/health`, `/predict`, `/train`, `/domains`). Almost all `backend/` Java classes (controllers, services, entities, repositories) are **empty stubs** — the backend boots and connects to PostgreSQL but exposes no business REST endpoints, only Spring Actuator (`GET /actuator/health`). The Angular `frontend/` is the default starter page with **empty routes** (`app.routes.ts` is `[]`). Keep this in mind before assuming an endpoint or page exists.

### PostgreSQL (required by backend)
- The dev DB is a native cluster (installed via apt). Start it each session before running the backend or backend tests:
  `sudo pg_ctlcluster 16 main start`
- Credentials/DB match `docker-compose.yml` and `application.properties` defaults: db `tracabilite_ia`, user `tracabilite`, password `tracabilite123`, port 5432.
- The backend's Spring context test (`./mvnw test`) needs Postgres running — it opens a real JDBC connection.

### Backend (`backend/`, port 8080)
- Run: `cd backend && ./mvnw spring-boot:run`  (build: `./mvnw -DskipTests package`; test: `./mvnw test`).
- Do NOT pass `-s settings.xml`: the repo's `backend/settings.xml` forces an insecure HTTP Maven mirror. The default `./mvnw` uses HTTPS Maven Central, which is what the update script and the commands above rely on.
- Note: `application.properties` excludes `SecurityAutoConfiguration`, but `SecurityConfig` (`@EnableWebSecurity`) is still active, so non-permitted endpoints require auth. `GET /actuator/health` is explicitly permitted and is the reliable liveness check.

### Frontend (`frontend/`, dev server)
- Run dev server: `cd frontend && npm start -- --port 5173 --host 0.0.0.0 --allowed-hosts`.
- `--allowed-hosts` (a boolean flag) is required: `angular.json` sets `security.allowedHosts: []`, so without it the Vite/SSR dev server returns HTTP 400 ("host ... is not allowed") for any request.
- Port 5173 matches the backend CORS allow-list in `application.properties` (the README's "React 5173" note is stale; the frontend is Angular). Angular's own default port is 4200.

### ML service (`ml-service/`, port 5000)
- Uses a local venv at `ml-service/.venv` (created by the update script). Run: `cd ml-service && .venv/bin/python app.py`.
- Trained model `.pkl` files are written under `ml-service/models/` at runtime.
