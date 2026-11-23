package com.aaminter.starbankrecommendation.service.cache;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Coordinates cache invalidation across all registered cache owners.
 */
@Service
public class CacheManagementService {

    private final List<CacheClearable> cacheOwners;

    /**
     * Creates the service with all cache-owning components registered by Spring.
     *
     * @param cacheOwners components whose caches can be invalidated
     */
    public CacheManagementService(List<CacheClearable> cacheOwners) {
        this.cacheOwners = List.copyOf(cacheOwners);
    }

    /**
     * Invokes cache invalidation on every registered cache owner.
     */
    public void clearAllCaches() {
        cacheOwners.forEach(CacheClearable::clearCache);
    }
}
