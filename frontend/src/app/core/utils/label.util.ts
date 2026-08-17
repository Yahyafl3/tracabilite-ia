import { StatutDecisionEnum } from '../models/decision.models';
import { UserRole } from '../models/auth.models';
import type { DecisionHistoryAction } from '../services/decision-trace.service';
import { i18nLabel } from '../i18n/label-translator';

export { bindLabelTranslator, i18nLabel } from '../i18n/label-translator';

function tr(key: string, fallback: string, params?: Record<string, unknown>): string {
  return i18nLabel(key, fallback, params);
}

const STATUT_LABELS: Record<StatutDecisionEnum, string> = {
  [StatutDecisionEnum.BROUILLON]: 'Brouillon',
  [StatutDecisionEnum.EN_ANALYSE]: 'En analyse',
  [StatutDecisionEnum.ANALYSEE]: 'Analysée',
  [StatutDecisionEnum.EN_ATTENTE_VALIDATION]: 'En attente de validation',
  [StatutDecisionEnum.EN_ATTENTE]: 'En attente',
  [StatutDecisionEnum.VALIDEE]: 'Validée',
  [StatutDecisionEnum.APPROUVEE]: 'Approuvée',
  [StatutDecisionEnum.MODIFIEE]: 'Modifiée',
  [StatutDecisionEnum.A_REVOIR]: 'À revoir',
  [StatutDecisionEnum.REJETEE]: 'Rejetée',
  [StatutDecisionEnum.ARCHIVEE]: 'Archivée',
};

const RISK_LABELS: Record<string, string> = {
  HIGH: 'Élevé',
  MEDIUM: 'Moyen',
  LOW: 'Faible',
  ELEVE: 'Élevé',
  MOYEN: 'Moyen',
  MODERE: 'Moyen',
  MODÉRÉ: 'Moyen',
  FAIBLE: 'Faible',
};

const HISTORY_ACTION_LABELS: Record<string, string> = {
  DECISION_CREATED: 'Décision créée',
  ML_ANALYSIS_STARTED: 'Analyse ML démarrée',
  ML_ANALYSIS_COMPLETED: 'Analyse ML terminée',
  ML_ANALYSIS_FAILED: 'Analyse ML échouée',
  OPENROUTER_ANALYSIS_STARTED: 'Consultation agents LLM',
  AGENT_RESPONSE_SUCCESS: 'Réponse agent reçue',
  AGENT_RESPONSE_FAILED: 'Échec agent LLM',
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
  const fallback = STATUT_LABELS[statut as StatutDecisionEnum] ?? String(statut);
  return tr(`status.${statut}`, fallback);
}

/** Libellé métier pour un niveau de risque. */
export function riskLabel(risk?: string | null): string {
  if (!risk) return tr('common.dash', '—');
  return tr(`riskCode.${risk}`, RISK_LABELS[risk] ?? risk);
}

/** Libellé métier pour une action d'historique de traçabilité. */
export function historyActionLabel(action: DecisionHistoryAction | string): string {
  const fallback = HISTORY_ACTION_LABELS[action] ?? action.replaceAll('_', ' ').toLowerCase();
  return tr(`history.${action}`, fallback);
}

/** Libellé court pour une décision ML (APPROUVER / REJETER). */
export function decisionLabel(decision?: string | null): string {
  if (!decision) return tr('common.dash', '—');
  if (decision === 'APPROUVER' || decision === 'REJETER' || decision === 'REVIEW') {
    return tr(`ml.${decision}`, decision === 'APPROUVER' ? 'Approuver' : decision === 'REJETER' ? 'Rejeter' : 'À revoir');
  }
  const human = tr(`humanDecision.${decision}`, '');
  return human || decision;
}

/** Libellé métier pour un domaine (CREDIT / MEDICAL / EDUCATION). */
export function domainLabel(domain?: string | null): string {
  if (!domain) return tr('common.dash', '—');
  return tr(`domainCode.${domain}`, domain);
}

/** Libellé métier pour un rôle utilisateur. */
export function roleLabel(role: UserRole | string): string {
  const mapped =
    role === UserRole.UTILISATEUR
      ? UserRole.AGENT_CREDIT
      : role === UserRole.VALIDATEUR
        ? UserRole.RESPONSABLE_CREDIT
        : String(role);
  const map: Record<string, string> = {
    [UserRole.ADMINISTRATEUR]: 'Administrateur',
    [UserRole.AUDITEUR]: 'Auditeur',
    [UserRole.AGENT_CREDIT]: 'Agent Crédit',
    [UserRole.AGENT_SANTE]: 'Agent Santé',
    [UserRole.AGENT_PEDAGOGIQUE]: 'Agent Pédagogique',
    [UserRole.RESPONSABLE_CREDIT]: 'Responsable Crédit',
    [UserRole.PROFESSIONNEL_SANTE]: 'Professionnel de Santé',
    [UserRole.RESPONSABLE_PEDAGOGIQUE]: 'Responsable Pédagogique',
    [UserRole.VALIDATEUR]: 'Responsable Crédit',
    [UserRole.UTILISATEUR]: 'Agent Crédit',
  };
  return tr(`roles.${mapped}`, map[String(role)] ?? String(role));
}

/** Classe CSS pour les chips de rôle. */
export function roleChipClass(role: UserRole | string): string {
  switch (role) {
    case UserRole.ADMINISTRATEUR:
      return 'chip--info';
    case UserRole.RESPONSABLE_CREDIT:
    case UserRole.PROFESSIONNEL_SANTE:
    case UserRole.RESPONSABLE_PEDAGOGIQUE:
    case UserRole.VALIDATEUR:
      return 'chip--approved';
    case UserRole.AGENT_CREDIT:
    case UserRole.AGENT_SANTE:
    case UserRole.AGENT_PEDAGOGIQUE:
    case UserRole.UTILISATEUR:
      return 'chip--pending';
    case UserRole.AUDITEUR:
      return 'chip--modified';
    default:
      return 'chip--pending';
  }
}
