export interface AgentResponse {
  reponseAgentId?: string;
  agentKey: string;
  modelId: string;
  modelName: string;
  provider: string;
  decisionProposee?: string;
  confianceDeclaree?: number;
  niveauRisque?: string;
  resume?: string;
  explication?: string;
  recommandations?: string[];
  dureeMs?: number;
  nombreTokens?: number;
  statut: 'SUCCESS' | 'FAILURE' | 'MODEL_UNAVAILABLE' | 'TIMEOUT';
  codeErreur?: string;
  timestamp?: string;
}

export interface ConsensusResponse {
  decisionConsensus?: string;
  confianceMoyenne?: number;
  agentsConsultes: number;
  agentsReussis: number;
  successfulAgentCount?: number;
  agreementRate?: number | null;
  consensusAvailable?: boolean;
  votes?: Record<string, number>;
  resume?: string;
  note?: string;
}

export interface OpenRouterModelStatus {
  agentKey: string;
  displayName: string;
  modelId: string;
  provider: string;
  configured: boolean;
  available: boolean;
  status: string;
}

export interface ConsensusDisplay {
  message: string;
  showDecisionBadge: boolean;
  decisionLabel?: string;
  agentsLabel: string;
}

export function successfulAgentCount(consensus: ConsensusResponse): number {
  return consensus.successfulAgentCount ?? consensus.agentsReussis ?? 0;
}

export function formatConsensusDisplay(consensus: ConsensusResponse): ConsensusDisplay {
  const agentsLabel = `${successfulAgentCount(consensus)}/${consensus.agentsConsultes} agents`;

  if (consensus.consensusAvailable === false) {
    if (consensus.decisionConsensus === 'INSUFFICIENT_RESPONSES' || successfulAgentCount(consensus) <= 1) {
      return {
        message: 'Consensus indisponible — nombre insuffisant de réponses réussies',
        showDecisionBadge: false,
        agentsLabel,
      };
    }

    return {
      message: 'Pas de consensus entre les agents',
      showDecisionBadge: false,
      agentsLabel,
    };
  }

  return {
    message: '',
    showDecisionBadge: true,
    decisionLabel: consensus.decisionConsensus,
    agentsLabel,
  };
}
