import { StatutDecisionEnum } from '../models/decision.models';

/** Classe CSS pour les chips de décision ML (APPROUVER / REJETER). */
export function decisionChipClass(decision?: string | null): string {
  if (decision === 'APPROUVER') return 'chip--approved';
  if (decision === 'REJETER') return 'chip--rejected';
  return 'chip--pending';
}

/** Classe CSS pour les chips de niveau de risque. */
export function riskChipClass(risk?: string | null): string {
  if (!risk) return 'chip--pending';
  const normalized = risk.toUpperCase().replace('É', 'E');
  if (normalized === 'HIGH' || normalized === 'ELEVE') return 'chip--rejected';
  if (normalized === 'MEDIUM' || normalized === 'MOYEN' || normalized === 'MODERE') return 'chip--modified';
  if (normalized === 'LOW' || normalized === 'FAIBLE') return 'chip--approved';
  return 'chip--pending';
}

/** Classe CSS pour les chips de statut de validation humaine. */
export function statutChipClass(statut: StatutDecisionEnum): string {
  const map: Record<StatutDecisionEnum, string> = {
    [StatutDecisionEnum.BROUILLON]: 'chip--pending',
    [StatutDecisionEnum.EN_ANALYSE]: 'chip--pending',
    [StatutDecisionEnum.ANALYSEE]: 'chip--modified',
    [StatutDecisionEnum.EN_ATTENTE_VALIDATION]: 'chip--pending',
    [StatutDecisionEnum.EN_ATTENTE]: 'chip--pending',
    [StatutDecisionEnum.VALIDEE]: 'chip--approved',
    [StatutDecisionEnum.APPROUVEE]: 'chip--approved',
    [StatutDecisionEnum.MODIFIEE]: 'chip--modified',
    [StatutDecisionEnum.A_REVOIR]: 'chip--modified',
    [StatutDecisionEnum.REJETEE]: 'chip--rejected',
    [StatutDecisionEnum.ARCHIVEE]: 'chip--pending',
  };
  return map[statut] ?? 'chip--pending';
}
