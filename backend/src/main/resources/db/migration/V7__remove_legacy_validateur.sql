-- Migration V7: Remove legacy VALIDATEUR role
-- This migration removes the VALIDATEUR role from the role constraint
-- and deletes the validateur@tracabilite.ia bootstrap account.
-- The VALIDATEUR role has been superseded by domain-specific validators:
-- RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE

-- Step 1: Delete the legacy validateur@tracabilite.ia account
DELETE FROM utilisateur
WHERE LOWER(email) = 'validateur@tracabilite.ia';

-- Step 2: Update the role check constraint to exclude VALIDATEUR
ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check;

ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
CHECK (role IN (
    'ADMINISTRATEUR',
    'AUDITEUR',
    'UTILISATEUR',
    'AGENT_CREDIT',
    'AGENT_SANTE',
    'AGENT_PEDAGOGIQUE',
    'RESPONSABLE_CREDIT',
    'PROFESSIONNEL_SANTE',
    'RESPONSABLE_PEDAGOGIQUE'
));

-- Step 3: Verification comment
-- After this migration:
-- - Total roles: 9 (reduced from 10)
-- - VALIDATEUR role is no longer valid in database
-- - validateur@tracabilite.ia account is deleted
-- - Domain-specific validators (credit@, sante@, pedago@) are preserved
-- - UTILISATEUR legacy role is preserved for backward compatibility
