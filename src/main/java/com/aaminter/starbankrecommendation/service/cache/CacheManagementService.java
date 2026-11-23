package com.aaminter.starbankrecommendation.service.cache;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Coordinates cache invalidation across all registered cache owners.
 */
@Service
public class CacheManagementService {

    private final List<CacheClearable> cacheOwners;

    public CacheManagementService(List<CacheClearable> cacheOwners) {
        this.cacheOwners = List.copyOf(cacheOwners);
    }

    public void clearAllCaches() {
        cacheOwners.forEach(CacheClearable::clearCache);
    }
}
