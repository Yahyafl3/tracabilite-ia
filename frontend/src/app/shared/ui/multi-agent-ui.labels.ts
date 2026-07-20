/** Libellés UI actifs pour le consensus multi-agents (hors données historiques). */
export const MULTI_AGENT_UI_LABELS = {
  consensus: 'Consensus multi-agents',
  agentResponses: 'Réponses des agents IA',
  synthesis: 'Synthèse multi-agents',
} as const;

/** Anciens libellés fournisseurs — ne doivent plus apparaître dans l’UI active. */
export const LEGACY_PROVIDER_UI_LABELS = [
  'Consensus OpenRouter',
  'Réponses des agents OpenRouter',
  'Synthèse agents OpenRouter',
  'Réponses des agents Groq',
  'Synthèse agents Groq',
  'Consensus agents (informatif)',
] as const;
