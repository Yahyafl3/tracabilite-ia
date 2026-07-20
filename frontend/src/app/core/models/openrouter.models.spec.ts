import { formatConsensusDisplay, formatDeclaredConfidence, successfulAgentCount, agentFallbackMessage, agentDisplayName, type ConsensusResponse, type AgentResponse } from './openrouter.models';

describe('consensus display', () => {
  const base: ConsensusResponse = {
    agentsConsultes: 3,
    agentsReussis: 0,
    successfulAgentCount: 0,
    consensusAvailable: false,
    decisionConsensus: 'INSUFFICIENT_RESPONSES',
  };

  it('shows unavailable message for 0 successful agents without REVIEW badge', () => {
    const display = formatConsensusDisplay(base);

    expect(display.showDecisionBadge).toBe(false);
    expect(display.message).toContain('Consensus indisponible');
    expect(display.decisionLabel).toBeUndefined();
    expect(display.agentsLabel).toBe('0/3 agents');
  });

  it('shows unavailable message for 1 successful agent', () => {
    const display = formatConsensusDisplay({
      ...base,
      agentsReussis: 1,
      successfulAgentCount: 1,
      decisionConsensus: 'INSUFFICIENT_RESPONSES',
    });

    expect(display.showDecisionBadge).toBe(false);
    expect(display.message).toContain('Consensus indisponible');
  });

  it('shows decision badge when consensus is available', () => {
    const display = formatConsensusDisplay({
      agentsConsultes: 3,
      agentsReussis: 2,
      successfulAgentCount: 2,
      consensusAvailable: true,
      decisionConsensus: 'APPROUVER',
      agreementRate: 100,
    });

    expect(display.showDecisionBadge).toBe(true);
    expect(display.decisionLabel).toBe('APPROUVER');
    expect(display.message).toBe('');
  });

  it('allows REVIEW only when returned by successful agents with majority', () => {
    const display = formatConsensusDisplay({
      agentsConsultes: 3,
      agentsReussis: 2,
      successfulAgentCount: 2,
      consensusAvailable: true,
      decisionConsensus: 'REVIEW',
      agreementRate: 66.67,
    });

    expect(display.showDecisionBadge).toBe(true);
    expect(display.decisionLabel).toBe('REVIEW');
  });

  it('shows no-consensus message when agents disagree', () => {
    const display = formatConsensusDisplay({
      agentsConsultes: 3,
      agentsReussis: 2,
      successfulAgentCount: 2,
      consensusAvailable: false,
      decisionConsensus: 'NO_CONSENSUS',
      agreementRate: 50,
    });

    expect(display.showDecisionBadge).toBe(false);
    expect(display.message).toBe('Pas de consensus entre les agents');
  });

  it('falls back to agentsReussis when successfulAgentCount is missing', () => {
    expect(successfulAgentCount({ agentsConsultes: 3, agentsReussis: 2 })).toBe(2);
  });

  it('shows Non fournie when declared confidence is null', () => {
    expect(formatDeclaredConfidence(null)).toBe('Non fournie');
    expect(formatDeclaredConfidence(undefined)).toBe('Non fournie');
  });

  it('formats declared confidence as percentage', () => {
    expect(formatDeclaredConfidence(0.8)).toBe('80 %');
    expect(formatDeclaredConfidence(0.9216)).toBe('92,16 %');
  });

  it('shows Valeur invalide for out-of-range declared confidence', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    expect(formatDeclaredConfidence(1.2)).toBe('Valeur invalide');
    expect(warn).toHaveBeenCalled();
    warn.mockRestore();
  });

  it('shows fallback message and actual model display name', () => {
    const agent: AgentResponse = {
      agentKey: 'AGENT_1',
      modelId: 'google/gemma-4-26b-a4b-it:free',
      actualModelId: 'google/gemma-4-26b-a4b-it:free',
      provider: 'GOOGLE_OPENROUTER',
      statut: 'SUCCESS',
      fallbackUsed: true,
    };

    expect(agentFallbackMessage(agent)).toContain('Modèle principal indisponible');
    expect(agentDisplayName(agent)).toBe('google/gemma-4-26b-a4b-it:free');
  });
});
