import type { ValidationActionResponse } from './validation.models';
import type { AgentResponse, ConsensusResponse } from './openrouter.models';

export enum StatutDecisionEnum {
  BROUILLON = 'BROUILLON',
  EN_ATTENTE = 'EN_ATTENTE',
  APPROUVEE = 'APPROUVEE',
  MODIFIEE = 'MODIFIEE',
  REJETEE = 'REJETEE',
}

export interface MlPredictionView {
  decision?: string;
  confidenceScore?: number;
  riskLevel?: string;
  modelName?: string;
  modelVersion?: string;
}

export interface ExplanationFactor {
  factorId?: string;
  name: string;
  value: string;
  shapValue: number;
  impact: 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';
  impactLabel?: string;
  explainedClass?: string;
  rank: number;
  contributionPercent: number;
  source: string;
}

export interface DecisionResponse {
  decisionId: string;
  reference?: string;
  prompt: string;
  contexte: string;
  modelName: string;
  modelVersion?: string;
  reponse: string;
  statutValidation: StatutDecisionEnum;
  suggestedDecision?: string;
  confidenceScore?: number;
  riskLevel?: string;
  explanationSource?: string;
  resumeConsensus?: string;
  consensus?: ConsensusResponse;
  consensusDecision?: string;
  mlPrediction?: MlPredictionView;
  agentResponses?: AgentResponse[];
  features?: Record<string, unknown>;
  probabilities?: Record<string, number>;
  factors?: ExplanationFactor[];
  validations?: ValidationActionResponse[];
  humanFinalDecision?: string;
  humanFinalAction?: 'APPROUVER' | 'REJETER' | 'MODIFIER' | 'REVIEW';
  validatorEmail?: string;
  validatedAt?: string;
  timestamp: string;
  currentHash?: string;
}

export interface DecisionPageResponse {
  content: DecisionResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export type { Sector } from '../config/sector-fields.config';
export { SECTORS } from '../config/sector-fields.config';

export interface CreditFeaturesRequest {
  amount: number;
  monthlyIncome: number;
  companyAgeYears: number;
  paymentIncidents: number;
  debtRatio: number;
  sector: import('../config/sector-fields.config').Sector;
  description?: string;
  includeOpenRouter?: boolean;
}

export function mlDecision(decision: DecisionResponse): string | undefined {
  return decision.mlPrediction?.decision ?? decision.suggestedDecision;
}

export function mlConfidence(decision: DecisionResponse): number | undefined {
  return decision.mlPrediction?.confidenceScore ?? decision.confidenceScore;
}

export function humanFinalLabel(decision: DecisionResponse): string | undefined {
  if (decision.humanFinalAction === 'MODIFIER') {
    return `MODIFIER → ${decision.humanFinalDecision ?? '-'}`;
  }
  if (decision.humanFinalAction === 'REVIEW') {
    return 'REVIEW';
  }
  return decision.humanFinalDecision;
}

export function consensusLabel(decision: DecisionResponse): string {
  if (!decision.consensusDecision) {
    return 'Indisponible';
  }
  if (decision.consensusDecision === 'NO_CONSENSUS') {
    return 'Pas de consensus';
  }
  if (decision.consensusDecision === 'INSUFFICIENT_RESPONSES') {
    return 'Réponses insuffisantes';
  }
  return decision.consensusDecision;
}
