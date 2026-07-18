import { resolveAgentError } from './agent-error.util';

describe('resolveAgentError', () => {
  it('maps RATE_LIMITED code to user message', () => {
    const result = resolveAgentError('FAILURE', 'RATE_LIMITED');

    expect(result).not.toBeNull();
    expect(result?.userMessage).toBe('Quota OpenRouter atteint. Réessayez dans quelques instants.');
    expect(result?.technicalLine).toBe('RATE_LIMITED');
  });

  it('maps RATE_LIMITED status to user message', () => {
    const result = resolveAgentError('RATE_LIMITED', null);

    expect(result).not.toBeNull();
    expect(result?.userMessage).toBe('Quota OpenRouter atteint. Réessayez dans quelques instants.');
    expect(result?.technicalLine).toBe('RATE_LIMITED');
  });
});
