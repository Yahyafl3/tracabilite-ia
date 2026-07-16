-- =============================================================================
-- Purge ciblée des décisions DEMO (ChatGPT, Claude, Gemini, Ollama/Qwen)
-- =============================================================================
-- Conserve les décisions ML/OpenRouter réelles (model_name != demo agents)
-- Usage: docker exec -i tracabilite-postgres psql -U tracabilite -d tracabilite_ia -f -
-- =============================================================================

\echo '=== DECISIONS DEMO IDENTIFIEES ==='
SELECT d.decision_id, d.prompt, d.model_name, si.nom AS systeme_ia
FROM decision d
LEFT JOIN systeme_ia si ON si.systeme_ia_id = d.systeme_ia_id
WHERE si.nom IN ('ChatGPT', 'Claude', 'Gemini', 'Ollama Local')
   OR lower(d.model_name) IN ('chatgpt', 'claude', 'gemini', 'ollama local', 'qwen3:4b', 'qwen3:0.6b')
   OR d.contexte LIKE 'Contexte de demonstration%'
   OR d.reponse LIKE 'Reponse de demo%'
ORDER BY d.timestamp;

\echo '=== COMPTAGE AVANT ==='
SELECT 'decision_demo' AS label, COUNT(*) FROM decision d
LEFT JOIN systeme_ia si ON si.systeme_ia_id = d.systeme_ia_id
WHERE si.nom IN ('ChatGPT', 'Claude', 'Gemini', 'Ollama Local')
   OR lower(d.model_name) IN ('chatgpt', 'claude', 'gemini', 'ollama local', 'qwen3:4b', 'qwen3:0.6b')
   OR d.contexte LIKE 'Contexte de demonstration%'
   OR d.reponse LIKE 'Reponse de demo%';

BEGIN;

CREATE TEMP TABLE demo_decisions AS
SELECT d.decision_id
FROM decision d
LEFT JOIN systeme_ia si ON si.systeme_ia_id = d.systeme_ia_id
WHERE si.nom IN ('ChatGPT', 'Claude', 'Gemini', 'Ollama Local')
   OR lower(d.model_name) IN ('chatgpt', 'claude', 'gemini', 'ollama local', 'qwen3:4b', 'qwen3:0.6b')
   OR d.contexte LIKE 'Contexte de demonstration%'
   OR d.reponse LIKE 'Reponse de demo%';

DELETE FROM validation_action WHERE decision_id IN (SELECT decision_id FROM demo_decisions);
DELETE FROM reponse_agent_ia WHERE decision_id IN (SELECT decision_id FROM demo_decisions);
DELETE FROM explanation_factor WHERE decision_id IN (SELECT decision_id FROM demo_decisions);
DELETE FROM decision_source WHERE decision_id IN (SELECT decision_id FROM demo_decisions);
DELETE FROM decision_history WHERE decision_id IN (SELECT decision_id FROM demo_decisions);
UPDATE decision SET decision_precedente_id = NULL WHERE decision_id IN (SELECT decision_id FROM demo_decisions);
DELETE FROM decision WHERE decision_id IN (SELECT decision_id FROM demo_decisions);

DELETE FROM systeme_ia
WHERE nom IN ('ChatGPT', 'Claude', 'Gemini', 'Ollama Local')
  AND systeme_ia_id NOT IN (SELECT DISTINCT systeme_ia_id FROM decision WHERE systeme_ia_id IS NOT NULL);

COMMIT;

\echo '=== COMPTAGE APRES ==='
SELECT 'decision' AS table_name, COUNT(*) FROM decision
UNION ALL SELECT 'decision_history', COUNT(*) FROM decision_history
UNION ALL SELECT 'decision_source', COUNT(*) FROM decision_source
UNION ALL SELECT 'systeme_ia', COUNT(*) FROM systeme_ia
UNION ALL SELECT 'utilisateur', COUNT(*) FROM utilisateur
ORDER BY 1;
