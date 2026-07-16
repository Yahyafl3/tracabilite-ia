-- =============================================================================
-- Nettoyage DEV : données de test des décisions
-- =============================================================================
-- Supprime :
--   - explanation_factor (facteurs SHAP)
--   - decision (chaîne de hash via decision_precedente_id)
--   - appel_ia (historique des appels Ollama générés pendant les tests)
--
-- Conserve :
--   - utilisateur, systeme_ia, trace_capture_job
--   - volumes PostgreSQL / Ollama (aucune action Docker)
--
-- Usage :
--   docker exec -i tracabilite-postgres psql -U tracabilite -d tracabilite_ia \
--     -f - < backend/scripts/clean-test-decisions.sql
--   ou via scripts/dev-clean-decisions.ps1
-- =============================================================================

\echo '=== AVANT SUPPRESSION ==='
SELECT 'decision' AS table_name, COUNT(*) AS row_count FROM decision
UNION ALL SELECT 'explanation_factor', COUNT(*) FROM explanation_factor
UNION ALL SELECT 'appel_ia', COUNT(*) FROM appel_ia
UNION ALL SELECT 'systeme_ia', COUNT(*) FROM systeme_ia
UNION ALL SELECT 'utilisateur', COUNT(*) FROM utilisateur
UNION ALL SELECT 'trace_capture_job', COUNT(*) FROM trace_capture_job
ORDER BY 1;

BEGIN;

-- 1. Validations humaines (FK validation_action.decision_id -> decision)
DELETE FROM validation_action;

-- 2. Reponses agents OpenRouter (FK reponse_agent_ia.decision_id -> decision)
DELETE FROM reponse_agent_ia;

-- 3. Sources et historique
DELETE FROM decision_source;
DELETE FROM decision_history;

-- 4. Facteurs SHAP (FK explanation_factor.decision_id -> decision)
DELETE FROM explanation_factor;

-- 5. Casser la chaîne de hash (auto-référence decision.decision_precedente_id)
UPDATE decision SET decision_precedente_id = NULL;

-- 6. Historique des appels IA (logs des tests, sans FK directe vers decision)
DELETE FROM appel_ia;

-- 7. Décisions de test
DELETE FROM decision;

COMMIT;

\echo '=== APRES SUPPRESSION ==='
SELECT 'decision' AS table_name, COUNT(*) AS row_count FROM decision
UNION ALL SELECT 'explanation_factor', COUNT(*) FROM explanation_factor
UNION ALL SELECT 'appel_ia', COUNT(*) FROM appel_ia
UNION ALL SELECT 'systeme_ia', COUNT(*) FROM systeme_ia
UNION ALL SELECT 'utilisateur', COUNT(*) FROM utilisateur
UNION ALL SELECT 'trace_capture_job', COUNT(*) FROM trace_capture_job
ORDER BY 1;

\echo '=== VERIFICATION ORPHELINS ==='
SELECT 'explanation_factor_orphelins' AS check_name, COUNT(*) AS count
FROM explanation_factor ef
LEFT JOIN decision d ON d.decision_id = ef.decision_id
WHERE d.decision_id IS NULL
UNION ALL
SELECT 'decision_chain_orphelins', COUNT(*)
FROM decision d
LEFT JOIN decision p ON p.decision_id = d.decision_precedente_id
WHERE d.decision_precedente_id IS NOT NULL AND p.decision_id IS NULL;
