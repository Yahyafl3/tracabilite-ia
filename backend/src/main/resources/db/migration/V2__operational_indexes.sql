-- Index opérationnels (no-op si tables absentes)
DO $$
BEGIN
  IF to_regclass('public.decision') IS NOT NULL THEN
    CREATE INDEX IF NOT EXISTS idx_decision_created_at ON decision(timestamp);
    CREATE INDEX IF NOT EXISTS idx_decision_validated_at ON decision(validated_at);
    CREATE INDEX IF NOT EXISTS idx_decision_created_by ON decision(created_by);
    CREATE INDEX IF NOT EXISTS idx_decision_validateur_id ON decision(validateur_id);
    CREATE INDEX IF NOT EXISTS idx_decision_domaine_statut ON decision(domaine, statut_validation);
  END IF;

  IF to_regclass('public.audit_log') IS NOT NULL THEN
    CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);
  END IF;

  IF to_regclass('public.decision_history') IS NOT NULL THEN
    CREATE INDEX IF NOT EXISTS idx_decision_history_decision ON decision_history(decision_id);
    CREATE INDEX IF NOT EXISTS idx_decision_history_created ON decision_history(created_at);
  END IF;
END $$;
