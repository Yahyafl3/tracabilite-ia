FROM python:3.11-slim

WORKDIR /app

COPY ml-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Legacy + multidomain sources
COPY ml-service/ ./
COPY datasets /datasets

COPY ml-service/docker-entrypoint.sh /app/docker-entrypoint.sh
RUN sed -i 's/\r//' /app/docker-entrypoint.sh && chmod +x /app/docker-entrypoint.sh

ENV PORT=5000
ENV DATASETS_DIR=/datasets
EXPOSE 5000
ENTRYPOINT ["./docker-entrypoint.sh"]
