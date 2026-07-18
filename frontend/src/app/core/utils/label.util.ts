import { StatutDecisionEnum } from '../models/decision.models';
import { UserRole } from '../models/auth.models';
import type { DecisionHistoryAction } from '../services/decision-trace.service';

const STATUT_LABELS: Record<StatutDecisionEnum, string> = {
  [StatutDecisionEnum.APPROUVEE]: 'Approuvée',
  [StatutDecisionEnum.MODIFIEE]: 'Modifiée',
  [StatutDecisionEnum.REJETEE]: 'Rejetée',
  [StatutDecisionEnum.EN_ATTENTE]: 'En attente',
  [StatutDecisionEnum.BROUILLON]: 'Brouillon',
};

const RISK_LABELS: Record<string, string> = {
  HIGH: 'Élevé',
  MEDIUM: 'Modéré',
  LOW: 'Faible',
};

const HISTORY_ACTION_LABELS: Record<string, string> = {
  DECISION_CREATED: 'Décision créée',
  ML_ANALYSIS_STARTED: 'Analyse ML démarrée',
  ML_ANALYSIS_COMPLETED: 'Analyse ML terminée',
  ML_ANALYSIS_FAILED: 'Analyse ML échouée',
  OPENROUTER_ANALYSIS_STARTED: 'Consultation agents OpenRouter',
  AGENT_RESPONSE_SUCCESS: 'Réponse agent reçue',
  AGENT_RESPONSE_FAILED: 'Échec agent OpenRouter',
  CONSENSUS_CALCULATED: 'Consensus calculé',
  DECISION_SUBMITTED_FOR_VALIDATION: 'Soumise à validation',
  DECISION_APPROVED: 'Approuvée par le validateur',
  DECISION_REJECTED: 'Rejetée par le validateur',
  DECISION_MODIFIED: 'Modifiée par le validateur',
  DECISION_REVIEWED: 'Revue demandée',
  SOURCE_ADDED: 'Source ajoutée',
  SOURCE_REMOVED: 'Source retirée',
  INTEGRITY_VERIFIED: 'Intégrité vérifiée',
  DECISION_ARCHIVED: 'Décision archivée',
};

/** Libellé métier lisible pour un statut de validation. */
export function statutLabel(statut: StatutDecisionEnum | string): string {
  return STATUT_LABELS[statut as StatutDecisionEnum] ?? statut;
}

/** Libellé métier pour un niveau de risque. */
export function riskLabel(risk?: string | null): string {
  if (!risk) return '—';
  return RISK_LABELS[risk] ?? risk;
}

/** Libellé métier pour une action d'historique de traçabilité. */
export function historyActionLabel(action: DecisionHistoryAction | string): string {
  return HISTORY_ACTION_LABELS[action] ?? action.replaceAll('_', ' ').toLowerCase();
}

/** Libellé court pour une décision ML (APPROUVER / REJETER). */
export function decisionLabel(decision?: string | null): string {
  if (decision === 'APPROUVER') return 'Approuver';
  if (decision === 'REJETER') return 'Rejeter';
  return decision ?? '—';
}

/** Libellé métier pour un rôle utilisateur. */
export function roleLabel(role: UserRole | string): string {
  const map: Record<string, string> = {
    [UserRole.ADMINISTRATEUR]: 'Administrateur',
    [UserRole.VALIDATEUR]: 'Validateur',
    [UserRole.AUDITEUR]: 'Auditeur',
    [UserRole.UTILISATEUR]: 'Utilisateur',
  };
  return map[String(role)] ?? String(role);
}

/** Classe CSS pour les chips de rôle. */
export function roleChipClass(role: UserRole | string): string {
  switch (role) {
    case UserRole.ADMINISTRATEUR:
      return 'chip--info';
    case UserRole.VALIDATEUR:
      return 'chip--approved';
    case UserRole.AUDITEUR:
      return 'chip--modified';
    case UserRole.UTILISATEUR:
      return 'chip--pending';
    default:
      return 'chip--pending';
  }
}
