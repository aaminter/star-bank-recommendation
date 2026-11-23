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

    public ManagementController(CacheManagementService cacheManagementService) {
        this.cacheManagementService = cacheManagementService;
    }

    @PostMapping("/clear-caches")
    public ResponseEntity<Void> clearCaches() {
        cacheManagementService.clearAllCaches();
        return ResponseEntity.noContent().build();
    }
}
