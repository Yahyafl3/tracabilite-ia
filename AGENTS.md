# AGENTS.md

## Cursor Cloud specific instructions

### What this project is
"Tracabilité IA" is a 3-tier + ML app in one repo. Full architecture is in `ARCHITECTURE.md`, `EXPLICATION_PROJET.md`, and `ML_INTEGRATION_TODO.md`. Note the root `README.md` is partly outdated (it calls the frontend "React + Vite" on port 5173 — it is actually **Angular** on port 4200).

Services and dev commands:

| Service | Path | Stack | Dev run command | Port |
|---|---|---|---|---|
| Database | (system) | PostgreSQL 16 | `sudo pg_ctlcluster 16 main start` | 5432 |
| Backend | `backend/` | Java 21, Spring Boot 3.4.1, Maven wrapper | `./mvnw -s settings.xml spring-boot:run` | 8080 |
| Frontend | `frontend/` | Angular 21 (Vite/SSR), npm | `npm start` | 4200 |
| ML service | `ml-service/` | Python 3.11, Flask + scikit-learn | `source .venv/bin/activate && python app.py` | 5000 |

### Current implementation state (important)
- **Backend and frontend are early-stage scaffolds.** Backend controllers (`backend/src/main/java/.../controller/*.java`) are empty stubs, so there are **no business REST endpoints yet** — only `GET /actuator/health` (returns `{"status":"UP"}`). The app root returns HTTP 403. Frontend routes/components are stubs; `http://localhost:4200/` renders the default Angular welcome page.
- **The ML service is the only fully-implemented business logic** (`ml-service/app.py`): `GET /health`, `GET /domains`, `POST /predict`, `POST /train`. It runs standalone; the backend→ML integration is still a TODO (`ML_INTEGRATION_TODO.md`), so nothing calls it automatically.

### Non-obvious setup/run caveats
- **PostgreSQL must be running before the backend starts** (Spring Boot fails fast without it). Credentials/db are defined in `docker-compose.yml` and `application.properties`: db `tracabilite_ia`, user `tracabilite`, password `tracabilite123`. Start the cluster with `sudo pg_ctlcluster 16 main start` (it is not auto-started on boot).
- **Backend Maven must use `-s settings.xml`** (repo pins an insecure Maven mirror + SSL-relax flags in `.mvn/maven.config`). If `./mvnw` fails with "permission denied", run `chmod +x backend/mvnw` or invoke as `sh ./mvnw`.
- Spring Security auto-config is disabled in `application.properties`, but Spring still logs a generated password on startup — that is expected, JWT auth is not enforced at runtime.
- **Frontend `npm install` requires `--legacy-peer-deps`** (peer-dependency conflicts). For local access `npm start` is enough. To expose it on all interfaces (e.g. for external testing) the Vite dev server blocks unknown hosts, so use `npm start -- --host 0.0.0.0 --allowed-hosts`.
- **The ML service needs Python 3.11**, not the system's 3.12: the pinned `numpy==1.26.2` / `scikit-learn==1.3.2` do not have clean wheels for 3.12. A venv lives at `ml-service/.venv`. ML models are lazily created on first `/predict` (or `/train`) into `ml-service/models/` — the `general` model is also created at startup.
- `docker-compose.yml` exists (postgres + all 3 services), but Docker is not installed in this environment; run the services natively as above.
