import { routes } from './app.routes';
import { MULTI_AGENT_UI_LABELS } from './shared/ui/multi-agent-ui.labels';

describe('Sakai migration finalize checks', () => {
  it('does not reference the removed CoreUI ShellComponent in routes', () => {
    const serialized = JSON.stringify(routes, (_key, value) =>
      typeof value === 'function' ? value.toString() : value,
    );
    expect(serialized).not.toMatch(/shell\.component/i);
    expect(serialized).not.toMatch(/ShellComponent/);
    expect(serialized).toMatch(/AppLayoutComponent/);
  });

  it('keeps OpenRouter marketing labels out of active UI label constants', () => {
    const active = Object.values(MULTI_AGENT_UI_LABELS);
    expect(active).not.toContain('Consensus OpenRouter');
    expect(active).not.toContain('Réponses OpenRouter');
    expect(active).not.toContain('Synthèse OpenRouter');
    expect(active).toContain('Consensus multi-agents');
  });
});
