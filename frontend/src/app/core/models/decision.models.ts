import type { ValidationActionResponse } from './validation.models';
import type { AgentResponse, ConsensusResponse } from './openrouter.models';

export enum StatutDecisionEnum {
  BROUILLON = 'BROUILLON',
  EN_ATTENTE = 'EN_ATTENTE',
  APPROUVEE = 'APPROUVEE',
  MODIFIEE = 'MODIFIEE',
  REJETEE = 'REJETEE',
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
  agentResponses?: AgentResponse[];
  features?: Record<string, unknown>;
  probabilities?: Record<string, number>;
  factors?: ExplanationFactor[];
  validations?: ValidationActionResponse[];
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
