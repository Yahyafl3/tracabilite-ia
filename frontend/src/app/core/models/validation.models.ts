import { StatutDecisionEnum } from './decision.models';

export enum TypeActionEnum {
  APPROUVER = 'APPROUVER',
  REJETER = 'REJETER',
  MODIFIER = 'MODIFIER',
  REVIEW = 'REVIEW',
}

export interface ValidationRequest {
  commentaire?: string;
  decisionHumaine?: 'APPROUVER' | 'REJETER';
}

export interface ValidationActionResponse {
  validationActionId: string;
  decisionId: string;
  validateurId: string;
  validateurNom: string;
  typeAction: TypeActionEnum;
  statutAvant: StatutDecisionEnum;
  statutApres: StatutDecisionEnum;
  decisionHumaine?: string;
  commentaire?: string;
  timestamp: string;
}
