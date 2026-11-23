package com.aaminter.starbankrecommendation.controller;

import com.aaminter.starbankrecommendation.service.cache.CacheManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Management endpoints for operational service actions.
 */
@RestController
@RequestMapping("/management")
public class ManagementController {

    private final CacheManagementService cacheManagementService;

    /**
     * Creates the controller with the cache management service.
     *
     * @param cacheManagementService service coordinating cache invalidation
     */
    public ManagementController(CacheManagementService cacheManagementService) {
        this.cacheManagementService = cacheManagementService;
    }

    /**
     * Invalidates all caches registered with the cache management service.
     *
     * @return {@code 204 No Content} after cache invalidation
     */
    @PostMapping("/clear-caches")
    public ResponseEntity<Void> clearCaches() {
        cacheManagementService.clearAllCaches();
        return ResponseEntity.noContent().build();
    }
}
