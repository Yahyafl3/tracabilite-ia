export interface AgentErrorDisplay {
  userMessage: string;
  technicalLine: string;
}

type AgentErrorKey =
  | 'RATE_LIMITED'
  | 'MODEL_UNAVAILABLE'
  | 'TIMEOUT'
  | 'INVALID_RESPONSE'
  | 'FAILURE';

const AGENT_ERROR_MAP: Record<AgentErrorKey, AgentErrorDisplay> = {
  RATE_LIMITED: {
    userMessage: 'Quota API temporairement atteint. Réessayez dans quelques instants.',
    technicalLine: 'RATE_LIMITED',
  },
  MODEL_UNAVAILABLE: {
    userMessage: 'Modèle indisponible. Un modèle de secours peut être utilisé.',
    technicalLine: 'MODEL_UNAVAILABLE',
  },
  TIMEOUT: {
    userMessage: 'Délai d\'attente dépassé lors de la consultation de l\'agent.',
    technicalLine: 'TIMEOUT',
  },
  INVALID_RESPONSE: {
    userMessage: 'Réponse agent invalide ou illisible.',
    technicalLine: 'INVALID_RESPONSE',
  },
  FAILURE: {
    userMessage: 'Échec de la consultation de l\'agent LLM.',
    technicalLine: 'FAILURE',
  },
};

function normalizeErrorKey(statut?: string | null, codeErreur?: string | null): AgentErrorKey | null {
  const code = (codeErreur ?? '').toUpperCase();
  const status = (statut ?? '').toUpperCase();

  if (code.includes('RATE_LIMITED') || status === 'RATE_LIMITED') {
    return 'RATE_LIMITED';
  }
  if (
    code.includes('TIMEOUT') ||
    status === 'TIMEOUT'
  ) {
    return 'TIMEOUT';
  }
  if (
    code === 'MODEL_UNAVAILABLE' ||
    code.includes('MODEL_NOT_FOUND') ||
    code.includes('UNAVAILABLE') ||
    status === 'MODEL_UNAVAILABLE'
  ) {
    return 'MODEL_UNAVAILABLE';
  }
  if (code.includes('INVALID_RESPONSE') || status === 'INVALID_RESPONSE') {
    return 'INVALID_RESPONSE';
  }
  if (status === 'FAILURE') {
    return 'FAILURE';
  }

  return null;
}

/** Mappe statut / codeErreur agent vers messages utilisateur et ligne technique. */
export function resolveAgentError(
  statut?: string | null,
  codeErreur?: string | null,
): AgentErrorDisplay | null {
  const key = normalizeErrorKey(statut, codeErreur);
  if (!key) {
    return null;
  }

  const base = AGENT_ERROR_MAP[key];
  const technicalLine = codeErreur?.trim() || base.technicalLine;

  return {
    userMessage: base.userMessage,
    technicalLine,
  };
}
