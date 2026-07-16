-- Corrige les anciennes décisions où 0 agent a réussi mais le consensus était REVIEW (fallback technique).
-- Colonne réelle : consensus_json (TEXT/JSON), compteur agents : agentsReussis dans le JSON.

UPDATE decision
SET consensus_json = (
    (consensus_json::jsonb
        - 'decisionConsensus'
        - 'agreementRate'
        - 'consensusAvailable'
        - 'successfulAgentCount')
    || jsonb_build_object(
        'decisionConsensus', 'INSUFFICIENT_RESPONSES',
        'agreementRate', NULL,
        'consensusAvailable', false,
        'successfulAgentCount', 0
    )
)::text
WHERE consensus_json IS NOT NULL
  AND (consensus_json::jsonb ->> 'agentsReussis')::int = 0
  AND consensus_json::jsonb ->> 'decisionConsensus' = 'REVIEW';

UPDATE decision
SET reponse = REPLACE(
    reponse,
    '[Consensus OpenRouter] REVIEW | agents reussis=0/3',
    '[Consensus OpenRouter] INSUFFICIENT_RESPONSES | agents reussis=0/3'
)
WHERE reponse LIKE '%[Consensus OpenRouter] REVIEW | agents reussis=0/3%';
