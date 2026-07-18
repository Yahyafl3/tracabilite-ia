package com.pfa.tracabilite_ia.openrouter;

import java.util.Locale;
import java.util.Map;

/**
 * Résout le nom affiché d'un agent à partir de son modelId OpenRouter.
 * Le displayName doit toujours correspondre au modelId réellement configuré.
 */
public final class OpenRouterAgentDisplayNames {

    private static final Map<String, String> KNOWN_NAMES = Map.ofEntries(
            Map.entry("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B"),
            Map.entry("nvidia/nemotron-3-nano-30b-a3b:free", "Nemotron 3 Nano 30B"),
            Map.entry("google/gemma-4-26b-a4b-it:free", "Gemma 4 26B A4B"),
            Map.entry("google/gemma-4-31b-it:free", "Gemma 4 31B"),
            Map.entry("openai/gpt-oss-20b:free", "GPT-OSS 20B"),
            Map.entry("openai/gpt-oss-120b:free", "GPT-OSS 120B")
    );

    private OpenRouterAgentDisplayNames() {
    }

    public static String resolve(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return "Inconnu";
        }
        String exact = KNOWN_NAMES.get(modelId.trim());
        if (exact != null) {
            return exact;
        }
        return deriveFromModelId(modelId.trim());
    }

    private static String deriveFromModelId(String modelId) {
        String lower = modelId.toLowerCase(Locale.ROOT);
        if (lower.contains("gemma-4-26b-a4b")) {
            return "Gemma 4 26B A4B";
        }
        if (lower.contains("gemma-4-31b")) {
            return "Gemma 4 31B";
        }
        if (lower.contains("llama-3.3-70b")) {
            return "Llama 3.3 70B";
        }
        if (lower.contains("gpt-oss-20b")) {
            return "GPT-OSS 20B";
        }
        if (lower.contains("nemotron-3-nano")) {
            return "Nemotron 3 Nano 30B";
        }
        int slash = modelId.lastIndexOf('/');
        String base = slash >= 0 ? modelId.substring(slash + 1) : modelId;
        int colon = base.indexOf(':');
        if (colon >= 0) {
            base = base.substring(0, colon);
        }
        return base.replace('-', ' ');
    }
}
