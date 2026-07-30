-- Migration multidomain — V1
-- Contexte: Hibernate ddl-auto=update applique aussi les changements d'entités.
-- Ce script documente et permet une migration manuelle contrôlée (Neon / PostgreSQL).
-- IMPORTANT: ne supprime pas les décisions existantes ; domaine = CREDIT par défaut.

BEGIN;

-- 1. Colonnes multidomain sur decision
ALTER TABLE decision ADD COLUMN IF NOT EXISTS domaine VARCHAR(32) DEFAULT 'CREDIT';
ALTER TABLE decision ADD COLUMN IF NOT EXISTS dossier_reference VARCHAR(64);
ALTER TABLE decision ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE decision ADD COLUMN IF NOT EXISTS dataset_version VARCHAR(128);
ALTER TABLE decision ADD COLUMN IF NOT EXISTS source_donnees VARCHAR(128);
ALTER TABLE decision ADD COLUMN IF NOT EXISTS accord_avec_ia BOOLEAN;
ALTER TABLE decision ADD COLUMN IF NOT EXISTS justification_humaine TEXT;
ALTER TABLE decision ADD COLUMN IF NOT EXISTS validateur_id UUID;
ALTER TABLE decision ADD COLUMN IF NOT EXISTS validateur_role VARCHAR(64);
ALTER TABLE decision ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP;
ALTER TABLE decision ADD COLUMN IF NOT EXISTS validated_at TIMESTAMP;
ALTER TABLE decision ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE decision ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- 2. Conversion des décisions existantes vers CREDIT
UPDATE decision SET domaine = 'CREDIT' WHERE domaine IS NULL OR domaine = '';

-- 3. Tables spécialisées
CREATE TABLE IF NOT EXISTS credit_decision_data (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL UNIQUE REFERENCES decision(decision_id),
    secteur_activite VARCHAR(32) NOT NULL,
    region VARCHAR(64) NOT NULL,
    age_demandeur INTEGER,
    statut_professionnel VARCHAR(32),
    revenu_mensuel_mad DOUBLE PRECISION,
    charges_mensuelles_mad DOUBLE PRECISION,
    montant_demande_mad DOUBLE PRECISION,
    duree_credit_mois INTEGER,
    anciennete_professionnelle_annees INTEGER,
    credits_existants INTEGER,
    incidents_paiement_24_mois INTEGER,
    ratio_endettement DOUBLE PRECISION,
    type_garantie VARCHAR(32),
    type_credit VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS medical_decision_data (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL UNIQUE REFERENCES decision(decision_id),
    region VARCHAR(64) NOT NULL,
    age INTEGER,
    sexe VARCHAR(16),
    imc DOUBLE PRECISION,
    niveau_activite_physique VARCHAR(32),
    antecedents_familiaux_diabete VARCHAR(8),
    hypertension VARCHAR(8),
    glycemie DOUBLE PRECISION,
    polyurie VARCHAR(8),
    polydipsie VARCHAR(8),
    perte_poids_soudaine VARCHAR(8),
    faiblesse VARCHAR(8),
    obesite VARCHAR(8),
    suivi_medical VARCHAR(8)
);

CREATE TABLE IF NOT EXISTS education_decision_data (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL UNIQUE REFERENCES decision(decision_id),
    region VARCHAR(64) NOT NULL,
    type_etablissement VARCHAR(48),
    filiere VARCHAR(32),
    niveau_etude VARCHAR(8),
    moyenne_semestre1 DOUBLE PRECISION,
    moyenne_semestre2 DOUBLE PRECISION,
    taux_absence DOUBLE PRECISION,
    modules_non_valides INTEGER,
    participation VARCHAR(16),
    bourse VARCHAR(8),
    distance_logement_km DOUBLE PRECISION,
    acces_internet VARCHAR(8),
    activite_professionnelle VARCHAR(8),
    historique_redoublement VARCHAR(8),
    situation_academique VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY,
    decision_id UUID,
    user_id UUID,
    user_role VARCHAR(64),
    action VARCHAR(64) NOT NULL,
    old_status VARCHAR(64),
    new_status VARCHAR(64),
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    correlation_id VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_decision_domaine ON decision(domaine);
CREATE INDEX IF NOT EXISTS idx_decision_statut ON decision(statut_validation);
CREATE INDEX IF NOT EXISTS idx_audit_log_decision ON audit_log(decision_id);

-- 4. Contraintes CHECK alignées sur les enums multidomaines
ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check;
ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
CHECK (role IN (
    'ADMINISTRATEUR', 'VALIDATEUR', 'AUDITEUR', 'UTILISATEUR',
    'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE'
));

ALTER TABLE decision DROP CONSTRAINT IF EXISTS decision_statut_validation_check;
ALTER TABLE decision ADD CONSTRAINT decision_statut_validation_check
CHECK (statut_validation IN (
    'BROUILLON', 'EN_ANALYSE', 'ANALYSEE', 'EN_ATTENTE_VALIDATION', 'EN_ATTENTE',
    'VALIDEE', 'APPROUVEE', 'MODIFIEE', 'A_REVOIR', 'REJETEE', 'ARCHIVEE'
));

ALTER TABLE decision_history DROP CONSTRAINT IF EXISTS decision_history_new_status_check;
ALTER TABLE decision_history DROP CONSTRAINT IF EXISTS decision_history_previous_status_check;
ALTER TABLE decision_history ADD CONSTRAINT decision_history_new_status_check
CHECK (new_status IS NULL OR new_status IN (
    'BROUILLON', 'EN_ANALYSE', 'ANALYSEE', 'EN_ATTENTE_VALIDATION', 'EN_ATTENTE',
    'VALIDEE', 'APPROUVEE', 'MODIFIEE', 'A_REVOIR', 'REJETEE', 'ARCHIVEE'
));
ALTER TABLE decision_history ADD CONSTRAINT decision_history_previous_status_check
CHECK (previous_status IS NULL OR previous_status IN (
    'BROUILLON', 'EN_ANALYSE', 'ANALYSEE', 'EN_ATTENTE_VALIDATION', 'EN_ATTENTE',
    'VALIDEE', 'APPROUVEE', 'MODIFIEE', 'A_REVOIR', 'REJETEE', 'ARCHIVEE'
));

COMMIT;
