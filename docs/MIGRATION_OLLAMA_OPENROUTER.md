# Migration Ollama → OpenRouter

## Contexte

Le projet utilisait initialement **Ollama** avec le modèle **Qwen** (port 11434) pour l'analyse générative locale.

Depuis la finalisation PFA, **OpenRouter** remplace entièrement Ollama en runtime :

- **Llama 3.3 70B** (`AGENT_1`)
- **Gemma 4 31B** (`AGENT_2`)
- **GPT-OSS 120B** (`AGENT_3`)

Une seule clé `OPENROUTER_API_KEY` (fichier `.env` local, jamais versionnée).

## Données historiques

Les anciennes décisions créées via Ollama restent en base avec :

- `provider = OLLAMA` ou `systeme_ia` = « Ollama Local »
- modèle `qwen3:*`
- réponses, hash et historique d'origine

Aucune **nouvelle** réponse Ollama n'est produite.

## Runtime actuel

| Composant | Statut |
|-----------|--------|
| Ollama / Qwen | Supprimé (code, Docker, scripts) |
| OpenRouter | Actif |
| LogisticRegression + SHAP | Actif (ml-service) |
| PostgreSQL | Source unique Dashboard / Comparaison |

## Démarrage

```powershell
docker compose up -d
```

Ne pas utiliser `docker compose down -v` (préserve PostgreSQL).
