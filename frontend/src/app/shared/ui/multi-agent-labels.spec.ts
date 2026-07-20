import { LEGACY_PROVIDER_UI_LABELS, MULTI_AGENT_UI_LABELS } from './multi-agent-ui.labels';

describe('libellés multi-agents génériques', () => {
  it('définit les libellés génériques attendus', () => {
    expect(MULTI_AGENT_UI_LABELS.consensus).toBe('Consensus multi-agents');
    expect(MULTI_AGENT_UI_LABELS.agentResponses).toBe('Réponses des agents IA');
    expect(MULTI_AGENT_UI_LABELS.synthesis).toBe('Synthèse multi-agents');
  });

  it('ne réutilise pas les libellés OpenRouter / Groq dans l’UI active', () => {
    const active = Object.values(MULTI_AGENT_UI_LABELS);
    for (const legacy of LEGACY_PROVIDER_UI_LABELS) {
      expect(active).not.toContain(legacy);
    }
  });
});
