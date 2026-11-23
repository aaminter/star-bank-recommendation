package com.aaminter.starbankrecommendation.service.cache;

/**
 * Implemented by components that own application caches.
 */
public interface CacheClearable {

    /**
     * Invalidates all entries held by the implementing component's cache.
     */
    void clearCache();
}
