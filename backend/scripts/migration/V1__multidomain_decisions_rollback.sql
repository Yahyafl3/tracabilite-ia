-- Rollback V1 multidomain — ATTENTION: destructif sur les nouvelles tables.
-- Ne pas exécuter en production sans sauvegarde.

BEGIN;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS education_decision_data;
DROP TABLE IF EXISTS medical_decision_data;
DROP TABLE IF EXISTS credit_decision_data;

ALTER TABLE decision DROP COLUMN IF EXISTS updated_at;
ALTER TABLE decision DROP COLUMN IF EXISTS created_by;
ALTER TABLE decision DROP COLUMN IF EXISTS validated_at;
ALTER TABLE decision DROP COLUMN IF EXISTS submitted_at;
ALTER TABLE decision DROP COLUMN IF EXISTS validateur_role;
ALTER TABLE decision DROP COLUMN IF EXISTS validateur_id;
ALTER TABLE decision DROP COLUMN IF EXISTS justification_humaine;
ALTER TABLE decision DROP COLUMN IF EXISTS accord_avec_ia;
ALTER TABLE decision DROP COLUMN IF EXISTS source_donnees;
ALTER TABLE decision DROP COLUMN IF EXISTS dataset_version;
ALTER TABLE decision DROP COLUMN IF EXISTS description;
ALTER TABLE decision DROP COLUMN IF EXISTS dossier_reference;
ALTER TABLE decision DROP COLUMN IF EXISTS domaine;

COMMIT;
