-- =============================================================================
-- reset-local-decisions.sql
-- Supprime UNIQUEMENT les décisions et données associées (environnement local).
-- Conserve : utilisateurs, rôles, comptes démo, configuration.
-- NE PAS exécuter contre Neon / Render / production.
-- =============================================================================

BEGIN;

-- 1) Compteurs avant
SELECT 'BEFORE decision' AS label, COUNT(*) AS n FROM decision;
SELECT 'BEFORE credit_decision_data' AS label, COUNT(*) AS n FROM credit_decision_data;
SELECT 'BEFORE medical_decision_data' AS label, COUNT(*) AS n FROM medical_decision_data;
SELECT 'BEFORE education_decision_data' AS label, COUNT(*) AS n FROM education_decision_data;
SELECT 'BEFORE audit_log' AS label, COUNT(*) AS n FROM audit_log;
SELECT 'BEFORE decision_history' AS label, COUNT(*) AS n FROM decision_history;
SELECT 'BEFORE reponse_agent_ia' AS label, COUNT(*) AS n FROM reponse_agent_ia;
SELECT 'BEFORE explanation_factor' AS label, COUNT(*) AS n FROM explanation_factor;
SELECT 'BEFORE decision_source' AS label, COUNT(*) AS n FROM decision_source;
SELECT 'BEFORE validation_action' AS label, COUNT(*) AS n FROM validation_action;
SELECT 'BEFORE utilisateur (must keep)' AS label, COUNT(*) AS n FROM utilisateur;

-- 2) Suppression enfants puis parents (ordre FK)
DELETE FROM decision_history;
DELETE FROM audit_log;
DELETE FROM validation_action;
DELETE FROM decision_source;
DELETE FROM explanation_factor;
DELETE FROM reponse_agent_ia;
DELETE FROM credit_decision_data;
DELETE FROM medical_decision_data;
DELETE FROM education_decision_data;
DELETE FROM decision;

-- 3) Compteurs après
SELECT 'AFTER decision' AS label, COUNT(*) AS n FROM decision;
SELECT 'AFTER credit_decision_data' AS label, COUNT(*) AS n FROM credit_decision_data;
SELECT 'AFTER medical_decision_data' AS label, COUNT(*) AS n FROM medical_decision_data;
SELECT 'AFTER education_decision_data' AS label, COUNT(*) AS n FROM education_decision_data;
SELECT 'AFTER audit_log' AS label, COUNT(*) AS n FROM audit_log;
SELECT 'AFTER decision_history' AS label, COUNT(*) AS n FROM decision_history;
SELECT 'AFTER reponse_agent_ia' AS label, COUNT(*) AS n FROM reponse_agent_ia;
SELECT 'AFTER explanation_factor' AS label, COUNT(*) AS n FROM explanation_factor;
SELECT 'AFTER decision_source' AS label, COUNT(*) AS n FROM decision_source;
SELECT 'AFTER validation_action' AS label, COUNT(*) AS n FROM validation_action;
SELECT 'AFTER utilisateur (must keep)' AS label, COUNT(*) AS n FROM utilisateur;

-- 4) Comptes conservés
SELECT email, role, actif FROM utilisateur ORDER BY role, email;

COMMIT;
