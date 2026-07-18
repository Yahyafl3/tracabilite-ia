package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.ai.client.OpenRouterClient;
import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cache en memoire de la liste GET /models (TTL configurable).
 */
@Service
public class OpenRouterModelsCacheService {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterModelsCacheService.class);

    private final OpenRouterClient openRouterClient;
    private final OpenRouterProperties properties;
    private final AtomicReference<CachedModels> cache = new AtomicReference<>();

    public OpenRouterModelsCacheService(OpenRouterClient openRouterClient,
                                        OpenRouterProperties properties) {
        this.openRouterClient = openRouterClient;
        this.properties = properties;
    }

    public Set<String> availableModelIds() {
        CachedModels current = cache.get();
        if (current != null && !current.isExpired(properties.getModelsCacheTtlMs())) {
            return current.modelIds();
        }
        return refresh();
    }

    public Set<String> refresh() {
        if (!properties.isConfigured()) {
            cache.set(CachedModels.empty());
            return Set.of();
        }
        try {
            Set<String> ids = openRouterClient.listAvailableModelIds();
            cache.set(new CachedModels(ids, Instant.now()));
            return ids;
        } catch (Exception ex) {
            log.warn("Echec refresh cache modeles OpenRouter: {}", ex.getMessage());
            CachedModels previous = cache.get();
            if (previous != null) {
                return previous.modelIds();
            }
            cache.set(CachedModels.empty());
            return Set.of();
        }
    }

    public void invalidate() {
        cache.set(null);
    }

    private record CachedModels(Set<String> modelIds, Instant fetchedAt) {
        static CachedModels empty() {
            return new CachedModels(Collections.emptySet(), Instant.EPOCH);
        }

        boolean isExpired(long ttlMs) {
            if (fetchedAt.equals(Instant.EPOCH)) {
                return true;
            }
            return Instant.now().isAfter(fetchedAt.plusMillis(Math.max(ttlMs, 1_000L)));
        }
    }
}
