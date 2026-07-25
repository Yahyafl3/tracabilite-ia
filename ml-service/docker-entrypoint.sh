#!/bin/sh
set -e

if [ ! -f artifacts/model.joblib ]; then
  echo "Aucun modele trouve, entrainement en cours..."
  python train_model.py
fi

PORT="${PORT:-5000}"
exec gunicorn --bind "0.0.0.0:${PORT}" --workers 1 --threads 2 --timeout 120 "app:app"
