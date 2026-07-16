FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY config.py feature_validator.py schemas.py sector_schema.py shap_config.py dataset_service.py train_model.py \
     model_loader.py prediction_service.py explainability_service.py test_shap_coherence.py \
     risk_service.py generate_csv.py app.py ./

# Le modèle est monté via volume (artifacts/) ou entraîné au 1er démarrage
COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

EXPOSE 5000
ENTRYPOINT ["./docker-entrypoint.sh"]
