-- Flyway V3 : schémas datasets publics (credit / medical / education)
-- Recrée les tables spécialisées avec les colonnes alignées sur ml-service/domain_schemas.py

DO $$
BEGIN
  IF to_regclass('public.decision') IS NULL THEN
    RAISE NOTICE 'V3 skipped: table decision absente';
    RETURN;
  END IF;
END $$;

DROP TABLE IF EXISTS credit_decision_data CASCADE;
DROP TABLE IF EXISTS medical_decision_data CASCADE;
DROP TABLE IF EXISTS education_decision_data CASCADE;

CREATE TABLE credit_decision_data (
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
    taux_endettement DOUBLE PRECISION
);

CREATE TABLE medical_decision_data (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL UNIQUE,
    age INTEGER,
    grossesses INTEGER,
    glycemie_mg_dl DOUBLE PRECISION,
    pression_arterielle_mmhg DOUBLE PRECISION,
    epaisseur_pli_cutane_mm DOUBLE PRECISION,
    insuline_micro_u_ml DOUBLE PRECISION,
    imc_kg_m2 DOUBLE PRECISION
);

CREATE TABLE education_decision_data (
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
    international VARCHAR(8)
);

DO $$
BEGIN
  IF to_regclass('public.decision') IS NOT NULL THEN
    ALTER TABLE credit_decision_data
        ADD CONSTRAINT fk_credit_decision_data_decision
        FOREIGN KEY (decision_id) REFERENCES decision(decision_id);
    ALTER TABLE medical_decision_data
        ADD CONSTRAINT fk_medical_decision_data_decision
        FOREIGN KEY (decision_id) REFERENCES decision(decision_id);
    ALTER TABLE education_decision_data
        ADD CONSTRAINT fk_education_decision_data_decision
        FOREIGN KEY (decision_id) REFERENCES decision(decision_id);
  END IF;
END $$;
