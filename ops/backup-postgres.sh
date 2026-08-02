#!/usr/bin/env bash
# Backup PostgreSQL local/Docker — aucun mot de passe en clair dans le script.
set -euo pipefail

ENV_NAME="${ENV_NAME:-local}"
CONTAINER="${POSTGRES_CONTAINER:-tracabilite-postgres}"
DB_NAME="${POSTGRES_DB:-tracabilite_ia}"
DB_USER="${POSTGRES_USER:-tracabilite}"
OUT_DIR="${BACKUP_DIR:-./backups}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
OUT_FILE="${OUT_DIR}/${ENV_NAME}-${DB_NAME}-${STAMP}.sql.gz"

mkdir -p "${OUT_DIR}"
echo "Environment: ${ENV_NAME}"
echo "Container:   ${CONTAINER}"
echo "Database:    ${DB_NAME}"
echo "Output:      ${OUT_FILE}"

if ! docker ps --format '{{.Names}}' | grep -qx "${CONTAINER}"; then
  echo "ERROR: container ${CONTAINER} not running" >&2
  exit 1
fi

docker exec -t "${CONTAINER}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" --no-owner --no-acl \
  | gzip > "${OUT_FILE}"

echo "Backup OK: ${OUT_FILE}"
ls -lh "${OUT_FILE}"
