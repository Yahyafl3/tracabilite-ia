#!/bin/sh
set -e

if [ ! -f artifacts/model.joblib ]; then
  echo "Aucun modele trouve, entrainement en cours..."
  python train_model.py
fi

exec python app.py
