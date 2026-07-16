import { formatConsensusDisplay, successfulAgentCount, type ConsensusResponse } from './openrouter.models';

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
});
