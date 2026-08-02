#!/usr/bin/env bash
# Restore PostgreSQL — confirmation obligatoire. Ne jamais viser Neon depuis ce script.
set -euo pipefail

ENV_NAME="${ENV_NAME:-local}"
CONTAINER="${POSTGRES_CONTAINER:-tracabilite-postgres}"
DB_NAME="${POSTGRES_DB:-tracabilite_ia}"
DB_USER="${POSTGRES_USER:-tracabilite}"
BACKUP_FILE="${1:-}"

if [[ -z "${BACKUP_FILE}" ]]; then
  echo "Usage: $0 <backup.sql.gz>" >&2
  exit 1
fi
if [[ ! -f "${BACKUP_FILE}" ]]; then
  echo "ERROR: fichier introuvable: ${BACKUP_FILE}" >&2
  exit 1
fi
if [[ "${ENV_NAME}" == "neon" || "${ENV_NAME}" == "production" ]]; then
  echo "ERROR: restauration refusée pour ENV_NAME=${ENV_NAME}" >&2
  exit 1
fi

echo "Environment: ${ENV_NAME}"
echo "Container:   ${CONTAINER}"
echo "Database:    ${DB_NAME}"
echo "Backup:      ${BACKUP_FILE}"
read -r -p "Confirmer la restauration DESTRUCTIVE locale (oui/non) : " CONFIRM
if [[ "${CONFIRM}" != "oui" ]]; then
  echo "Annulé."
  exit 0
fi

gunzip -c "${BACKUP_FILE}" | docker exec -i "${CONTAINER}" psql -U "${DB_USER}" -d "${DB_NAME}"
echo "Restore terminé."
