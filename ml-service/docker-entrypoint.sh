#!/bin/sh
set -e

# Legacy credit model (compat /predict)
if [ ! -f artifacts/model.joblib ]; then
  echo "Aucun modele legacy trouve, entrainement en cours..."
  python train_model.py
fi

DATASETS_ROOT="${DATASETS_DIR:-/datasets}"
if [ ! -d "$DATASETS_ROOT/credit" ]; then
  DATASETS_ROOT="../datasets"
fi

# Multidomain models
if [ ! -f models/credit/credit_pipeline.joblib ] \
  || [ ! -f models/medical/medical_pipeline.joblib ] \
  || [ ! -f models/education/education_pipeline.joblib ]; then
  echo "Modeles multidomain manquants — generation datasets + entrainement..."
  if [ ! -f "$DATASETS_ROOT/credit/credit_maroc_synthetic.csv" ]; then
    python scripts/generate_moroccan_datasets.py --seed 42 --output-dir "$DATASETS_ROOT"
  fi
  if [ ! -d ../datasets ] && [ -d "$DATASETS_ROOT" ]; then
    mkdir -p ..
    ln -sfn "$DATASETS_ROOT" ../datasets || cp -R "$DATASETS_ROOT" ../datasets
  fi
  python training/train_credit_model.py
  python training/train_medical_model.py
  python training/train_education_model.py
fi

PORT="${PORT:-5000}"
exec gunicorn --bind "0.0.0.0:${PORT}" --workers 1 --threads 2 --timeout 120 "app:app"
