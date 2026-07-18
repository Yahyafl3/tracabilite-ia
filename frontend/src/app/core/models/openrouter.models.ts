export interface AgentResponse {
  reponseAgentId?: string;
  agentKey: string;
  modelId: string;
  displayName?: string;
  modelName?: string;
  provider: string;
  decisionProposee?: string;
  declaredConfidence?: number | null;
  confianceDeclaree?: number | null;
  niveauRisque?: string;
  resume?: string;
  explication?: string;
  recommandations?: string[];
  dureeMs?: number;
  nombreTokens?: number;
  statut: 'SUCCESS' | 'INVALID_RESPONSE' | 'FAILURE' | 'MODEL_UNAVAILABLE' | 'TIMEOUT';
  displayStatus?: string;
  codeErreur?: string;
  requestedModelId?: string;
  actualModelId?: string;
  fallbackUsed?: boolean;
  fallbackReason?: string;
  responseHash?: string;
  retryCount?: number;
  fallbackMessage?: string;
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

export function agentDisplayName(agent: AgentResponse): string {
  if (agent.fallbackUsed && agent.actualModelId) {
    return agent.actualModelId;
  }
  return agent.displayName ?? agent.modelName ?? agent.agentKey;
}

export function agentFallbackMessage(agent: AgentResponse): string | null {
  if (!agent.fallbackUsed) {
    return null;
  }
  return agent.fallbackMessage
    ?? 'Modèle principal indisponible — réponse produite par le modèle de secours';
}

export function agentDeclaredConfidence(agent: AgentResponse): number | null | undefined {
  if (agent.declaredConfidence !== undefined) {
    return agent.declaredConfidence;
  }
  return agent.confianceDeclaree;
}

export function formatDeclaredConfidence(confidence?: number | null): string {
  if (confidence == null) {
    return 'Non fournie';
  }
  return `${(confidence * 100).toFixed(1)} %`;
}

export function agentByKey(agents: AgentResponse[] | undefined, agentKey: string): AgentResponse | undefined {
  return agents?.find((agent) => agent.agentKey === agentKey);
}

export function agentStatusLabel(agent?: AgentResponse): string {
  if (!agent) {
    return 'Non consulté';
  }
  return agent.displayStatus ?? agent.statut;
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
