# Flyway Staging Validation

## État

**NOT_TESTED** — aucune base PostgreSQL staging dédiée disponible dans cette session.
**Neon production / cloud :** non touché (règle absolue).

## Prérequis avant exécution

1. URL JDBC contenant explicitement `staging` (ex. DB name `tracabilite_staging`).
2. Backup réussi (`ops/backup-postgres.ps1` ou équivalent cloud).
3. `SPRING_PROFILES_ACTIVE` ≠ prod Neon.

## Procédure (à exécuter plus tard)

```bash
# 1. Vérifier URL
echo $SPRING_DATASOURCE_URL   # doit contenir staging

# 2. Backup
# ...

# 3. Info / migrate (depuis image backend ou mvn)
java -jar app.jar --spring.profiles.active=prod \
  --spring.flyway.enabled=true \
  --spring.jpa.hibernate.ddl-auto=validate
# ou
./mvnw -Dflyway.url=... -Dflyway.user=... -Dflyway.password=... flyway:info
./mvnw ... flyway:migrate
./mvnw ... flyway:validate
```

Migrations versionnées déjà dans le repo :

- `backend/src/main/resources/db/migration/V1__multidomain_decisions.sql`
- `backend/src/main/resources/db/migration/V2__operational_indexes.sql`

## Rollback

Voir `docs/DATABASE_MIGRATION_RUNBOOK.md` et `backend/scripts/migration/V1__multidomain_decisions_rollback.sql` (manuel).

## Décision RC

Bloqué **uniquement** par absence de staging réel — acceptable pour `PRODUCTION_RELEASE_CANDIDATE` technique selon critères demandés, tant que documenté.
