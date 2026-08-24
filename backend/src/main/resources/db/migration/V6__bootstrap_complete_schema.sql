-- Migration V6: Bootstrap complete schema
-- Creates all 15 JPA-managed tables from scratch
-- Idempotent, forward-only, non-destructive
-- Derived directly from JPA entity mappings

-- =====================================================
-- 1. UTILISATEUR (base user table)
-- =====================================================
CREATE TABLE IF NOT EXISTS utilisateur (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT true,
    date_creation TIMESTAMP
);

-- Add role check constraint with all 10 roles (V5 requirement)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'utilisateur_role_check' 
        AND conrelid = 'utilisateur'::regclass
    ) THEN
        ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
        CHECK (role IN (
            'ADMINISTRATEUR',
            'VALIDATEUR',
            'AUDITEUR',
            'UTILISATEUR',
            'AGENT_CREDIT',
            'AGENT_SANTE',
            'AGENT_PEDAGOGIQUE',
            'RESPONSABLE_CREDIT',
            'PROFESSIONNEL_SANTE',
            'RESPONSABLE_PEDAGOGIQUE'
        ));
    END IF;
END $$;

-- =====================================================
-- 2. SYSTEME_IA
-- =====================================================
CREATE TABLE IF NOT EXISTS systeme_ia (
    systeme_ia_id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    fournisseur VARCHAR(255) NOT NULL,
    modele VARCHAR(255) NOT NULL,
    version_modele VARCHAR(255),
    description TEXT,
    actif BOOLEAN NOT NULL DEFAULT true,
    date_creation TIMESTAMP NOT NULL,
    date_modification TIMESTAMP,
    CONSTRAINT uk_systeme_ia_nom_fournisseur UNIQUE (nom, fournisseur)
);

-- =====================================================
-- 3. DECISION (main decision table with all V1-V5 fields)
-- =====================================================
CREATE TABLE IF NOT EXISTS decision (
    decision_id UUID PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    prompt TEXT NOT NULL,
    contexte TEXT,
    model_name VARCHAR(255) NOT NULL,
    model_version VARCHAR(255),
    reponse TEXT NOT NULL,
    systeme_ia_id UUID,
    statut_validation VARCHAR(255) NOT NULL DEFAULT 'EN_ATTENTE',
    previous_hash VARCHAR(64),
    current_hash VARCHAR(64),
    features_json TEXT,
    suggested_decision VARCHAR(255),
    confidence_score DOUBLE PRECISION,
    risk_level VARCHAR(255),
    probabilities_json TEXT,
    resume_ollama TEXT,
    consensus_json TEXT,
    explanation_source VARCHAR(255),
    decision_precedente_id UUID,
    business_data_hash VARCHAR(64),
    sources_hash VARCHAR(64),
    agent_responses_hash VARCHAR(64),
    human_decision VARCHAR(32),
    validator_email VARCHAR(255),
    domaine VARCHAR(32) DEFAULT 'CREDIT',
    dossier_reference VARCHAR(64),
    description TEXT,
    dataset_version VARCHAR(128),
    source_donnees VARCHAR(128),
    accord_avec_ia BOOLEAN,
    justification_humaine TEXT,
    validateur_id UUID,
    validateur_role VARCHAR(64),
    submitted_at TIMESTAMP,
    validated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    CONSTRAINT fk_decision_systeme_ia FOREIGN KEY (systeme_ia_id) REFERENCES systeme_ia(systeme_ia_id),
    CONSTRAINT fk_decision_precedente FOREIGN KEY (decision_precedente_id) REFERENCES decision(decision_id)
);

-- Add decision status check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'decision_statut_validation_check' 
        AND conrelid = 'decision'::regclass
    ) THEN
        ALTER TABLE decision ADD CONSTRAINT decision_statut_validation_check
        CHECK (statut_validation IN (
            'BROUILLON', 'EN_ANALYSE', 'ANALYSEE', 'EN_ATTENTE_VALIDATION', 
            'EN_ATTENTE', 'VALIDEE', 'APPROUVEE', 'MODIFIEE', 'A_REVOIR', 
            'REJETEE', 'ARCHIVEE'
        ));
    END IF;
END $$;

-- =====================================================
-- 4. APPEL_IA (AI call tracing)
-- =====================================================
CREATE TABLE IF NOT EXISTS appel_ia (
    appel_ia_id UUID PRIMARY KEY,
    provider VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    model_version VARCHAR(255),
    system_prompt TEXT NOT NULL,
    user_prompt TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    duration_ms BIGINT,
    statut VARCHAR(255) NOT NULL,
    response TEXT,
    error_message TEXT,
    correlation_id VARCHAR(255) NOT NULL,
    utilisateur_id UUID,
    CONSTRAINT fk_appel_ia_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

CREATE INDEX IF NOT EXISTS idx_appel_ia_correlation ON appel_ia(correlation_id);
CREATE INDEX IF NOT EXISTS idx_appel_ia_utilisateur ON appel_ia(utilisateur_id);

-- =====================================================
-- 5. DECISION_HISTORY
-- =====================================================
CREATE TABLE IF NOT EXISTS decision_history (
    history_id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    action VARCHAR(255) NOT NULL,
    previous_status VARCHAR(255),
    new_status VARCHAR(255),
    performed_by_id UUID,
    performed_by_email VARCHAR(255),
    comment TEXT,
    justification TEXT,
    event_data_json TEXT,
    correlation_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_decision_history_decision FOREIGN KEY (decision_id) REFERENCES decision(decision_id)
);

-- Add decision_history status check constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'decision_history_new_status_check' 
        AND conrelid = 'decision_history'::regclass
    ) THEN
        ALTER TABLE decision_history ADD CONSTRAINT decision_history_new_status_check
        CHECK (new_status IS NULL OR new_status IN (
            'BROUILLON', 'EN_ANALYSE', 'ANALYSEE', 'EN_ATTENTE_VALIDATION', 
            'EN_ATTENTE', 'VALIDEE', 'APPROUVEE', 'MODIFIEE', 'A_REVOIR', 
            'REJETEE', 'ARCHIVEE'
        ));
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'decision_history_previous_status_check' 
        AND conrelid = 'decision_history'::regclass
    ) THEN
        ALTER TABLE decision_history ADD CONSTRAINT decision_history_previous_status_check
        CHECK (previous_status IS NULL OR previous_status IN (
            'BROUILLON', 'EN_ANALYSE', 'ANALYSEE', 'EN_ATTENTE_VALIDATION', 
            'EN_ATTENTE', 'VALIDEE', 'APPROUVEE', 'MODIFIEE', 'A_REVOIR', 
            'REJETEE', 'ARCHIVEE'
        ));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_decision_history_decision ON decision_history(decision_id);
CREATE INDEX IF NOT EXISTS idx_decision_history_created ON decision_history(created_at);

-- =====================================================
-- 6. REPONSE_AGENT_IA
-- =====================================================
CREATE TABLE IF NOT EXISTS reponse_agent_ia (
    reponse_agent_id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    agent_key VARCHAR(32) NOT NULL,
    model_id VARCHAR(255) NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    provider VARCHAR(255) NOT NULL,
    requested_model_id VARCHAR(255),
    actual_model_id VARCHAR(255),
    fallback_used BOOLEAN,
    fallback_reason VARCHAR(64),
    response_hash VARCHAR(255),
    retry_count INTEGER,
    reponse_brute TEXT,
    reponse_normalisee TEXT,
    decision_proposee VARCHAR(255),
    confiance_declaree DOUBLE PRECISION,
    niveau_risque VARCHAR(255),
    resume TEXT,
    explication TEXT,
    recommandations_json TEXT,
    duree_ms BIGINT,
    nombre_tokens INTEGER,
    statut VARCHAR(255) NOT NULL,
    code_erreur VARCHAR(64),
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_reponse_agent_ia_decision FOREIGN KEY (decision_id) REFERENCES decision(decision_id)
);

-- =====================================================
-- 7. VALIDATION_ACTION
-- =====================================================
CREATE TABLE IF NOT EXISTS validation_action (
    validation_action_id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    validateur_id UUID NOT NULL,
    type_action VARCHAR(255) NOT NULL,
    statut_avant VARCHAR(255) NOT NULL,
    statut_apres VARCHAR(255) NOT NULL,
    decision_humaine VARCHAR(32),
    commentaire TEXT,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_validation_action_decision FOREIGN KEY (decision_id) REFERENCES decision(decision_id),
    CONSTRAINT fk_validation_action_validateur FOREIGN KEY (validateur_id) REFERENCES utilisateur(id)
);

-- =====================================================
-- 8. CREDIT_DECISION_DATA
-- =====================================================
CREATE TABLE IF NOT EXISTS credit_decision_data (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL UNIQUE,
    age INTEGER,
    duree_mois INTEGER,
    type_contrat VARCHAR(32),
    statut_logement VARCHAR(32),
    incident_paiement_bam INTEGER,
    montant_demande_mad DOUBLE PRECISION,
    nouvelle_echeance_mad DOUBLE PRECISION,
    revenu_mensuel_mad DOUBLE PRECISION,
    taux_endettement DOUBLE PRECISION,
    CONSTRAINT fk_credit_decision_data_decision FOREIGN KEY (decision_id) REFERENCES decision(decision_id)
);

-- =====================================================
-- 9. MEDICAL_DECISION_DATA
-- =====================================================
CREATE TABLE IF NOT EXISTS medical_decision_data (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL UNIQUE,
    age INTEGER,
    grossesses INTEGER,
    glycemie_mg_dl DOUBLE PRECISION,
    pression_arterielle_mmhg DOUBLE PRECISION,
    epaisseur_pli_cutane_mm DOUBLE PRECISION,
    insuline_micro_u_ml DOUBLE PRECISION,
    imc_kg_m2 DOUBLE PRECISION,
    CONSTRAINT fk_medical_decision_data_decision FOREIGN KEY (decision_id) REFERENCES decision(decision_id)
);

-- =====================================================
-- 10. EDUCATION_DECISION_DATA
-- =====================================================
CREATE TABLE IF NOT EXISTS education_decision_data (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL UNIQUE,
    age_inscription INTEGER,
    note_admission DOUBLE PRECISION,
    note_qualification_precedente DOUBLE PRECISION,
    unites_validees_s1 INTEGER,
    moyenne_s1 DOUBLE PRECISION,
    unites_validees_s2 INTEGER,
    moyenne_s2 DOUBLE PRECISION,
    taux_chomage DOUBLE PRECISION,
    taux_inflation DOUBLE PRECISION,
    pib DOUBLE PRECISION,
    sexe VARCHAR(16),
    boursier VARCHAR(8),
    frais_a_jour VARCHAR(8),
    debiteur VARCHAR(8),
    deplace VARCHAR(8),
    international VARCHAR(8),
    CONSTRAINT fk_education_decision_data_decision FOREIGN KEY (decision_id) REFERENCES decision(decision_id)
);

-- =====================================================
-- 11. EXPLANATION_FACTOR
-- =====================================================
CREATE TABLE IF NOT EXISTS explanation_factor (
    factor_id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    shap_value DOUBLE PRECISION NOT NULL,
    impact VARCHAR(255) NOT NULL,
    rank INTEGER NOT NULL,
    contribution_percent DOUBLE PRECISION NOT NULL,
    source VARCHAR(255) NOT NULL,
    CONSTRAINT fk_explanation_factor_decision FOREIGN KEY (decision_id) REFERENCES decision(decision_id)
);

-- =====================================================
-- 12. DECISION_SOURCE
-- =====================================================
CREATE TABLE IF NOT EXISTS decision_source (
    source_id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    source_type VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    url TEXT,
    document_reference VARCHAR(512),
    content_hash VARCHAR(64),
    metadata_json TEXT,
    created_by_id UUID,
    created_by_email VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_decision_source_decision FOREIGN KEY (decision_id) REFERENCES decision(decision_id)
);

-- =====================================================
-- 13. AUDIT_LOG
-- =====================================================
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

CREATE INDEX IF NOT EXISTS idx_audit_log_decision ON audit_log(decision_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);

-- =====================================================
-- 14. PASSWORD_RESET_TOKEN
-- =====================================================
CREATE TABLE IF NOT EXISTS password_reset_token (
    id UUID PRIMARY KEY,
    utilisateur_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_reset_token_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_password_reset_token_hash ON password_reset_token(token_hash);

-- =====================================================
-- 15. SUPPORT_MESSAGE
-- =====================================================
CREATE TABLE IF NOT EXISTS support_message (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(120) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    processed_by_id UUID,
    CONSTRAINT fk_support_message_processed_by FOREIGN KEY (processed_by_id) REFERENCES utilisateur(id)
);

CREATE INDEX IF NOT EXISTS idx_support_message_status ON support_message(status);
CREATE INDEX IF NOT EXISTS idx_support_message_email ON support_message(email);
CREATE INDEX IF NOT EXISTS idx_support_message_created_at ON support_message(created_at);

-- =====================================================
-- OPERATIONAL INDEXES (V2 requirements)
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_decision_created_at ON decision(timestamp);
CREATE INDEX IF NOT EXISTS idx_decision_validated_at ON decision(validated_at);
CREATE INDEX IF NOT EXISTS idx_decision_created_by ON decision(created_by);
CREATE INDEX IF NOT EXISTS idx_decision_validateur_id ON decision(validateur_id);
CREATE INDEX IF NOT EXISTS idx_decision_domaine ON decision(domaine);
CREATE INDEX IF NOT EXISTS idx_decision_statut ON decision(statut_validation);
CREATE INDEX IF NOT EXISTS idx_decision_domaine_statut ON decision(domaine, statut_validation);
