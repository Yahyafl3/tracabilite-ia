import { resolveAgentError } from './agent-error.util';

describe('resolveAgentError', () => {
  it('maps RATE_LIMITED code to user message', () => {
    const result = resolveAgentError('FAILURE', 'OPENROUTER_RATE_LIMITED');

    expect(result?.userMessage).toContain('Quota API');
    expect(result?.technicalLine).toBe('OPENROUTER_RATE_LIMITED');
  });

  it('maps MODEL_UNAVAILABLE status', () => {
    const result = resolveAgentError('MODEL_UNAVAILABLE', null);

    expect(result?.userMessage).toContain('Modèle indisponible');
    expect(result?.technicalLine).toBe('MODEL_UNAVAILABLE');
  });

  it('maps TIMEOUT status', () => {
    const result = resolveAgentError('TIMEOUT', 'OPENROUTER_TIMEOUT');

    expect(result?.userMessage).toContain('Délai');
    expect(result?.technicalLine).toBe('OPENROUTER_TIMEOUT');
  });

  it('maps INVALID_RESPONSE code', () => {
    const result = resolveAgentError('INVALID_RESPONSE', 'INVALID_RESPONSE');

    expect(result?.userMessage).toContain('invalide');
  });

  it('maps FAILURE status', () => {
    const result = resolveAgentError('FAILURE', null);

    expect(result?.userMessage).toContain('Échec');
  });
});
