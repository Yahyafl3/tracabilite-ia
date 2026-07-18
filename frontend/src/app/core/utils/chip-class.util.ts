import { StatutDecisionEnum } from '../models/decision.models';

/** Classe CSS pour les chips de décision ML (APPROUVER / REJETER). */
export function decisionChipClass(decision?: string | null): string {
  if (decision === 'APPROUVER') return 'chip--approved';
  if (decision === 'REJETER') return 'chip--rejected';
  return 'chip--pending';
}

/** Classe CSS pour les chips de niveau de risque. */
export function riskChipClass(risk?: string | null): string {
  if (risk === 'HIGH') return 'chip--rejected';
  if (risk === 'MEDIUM') return 'chip--modified';
  if (risk === 'LOW') return 'chip--approved';
  return 'chip--pending';
}

/** Classe CSS pour les chips de statut de validation humaine. */
export function statutChipClass(statut: StatutDecisionEnum): string {
  const map: Record<StatutDecisionEnum, string> = {
    [StatutDecisionEnum.APPROUVEE]: 'chip--approved',
    [StatutDecisionEnum.MODIFIEE]: 'chip--modified',
    [StatutDecisionEnum.REJETEE]: 'chip--rejected',
    [StatutDecisionEnum.EN_ATTENTE]: 'chip--pending',
    [StatutDecisionEnum.BROUILLON]: 'chip--pending',
  };
  return map[statut] ?? 'chip--pending';
}
