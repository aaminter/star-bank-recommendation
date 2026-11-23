package com.aaminter.starbankrecommendation.controller;

import com.aaminter.starbankrecommendation.dto.RecommendationResponse;
import com.aaminter.starbankrecommendation.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST endpoint for retrieving personalized product recommendations.
 */
@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Creates the controller with the recommendation service.
     *
     * @param recommendationService service that evaluates recommendation rules
     */
    public RecommendationController(
            RecommendationService recommendationService
    ) {
        this.recommendationService = recommendationService;
    }

    /**
     * Builds recommendations for the customer identified by UUID.
     *
     * @param userId UUID of the customer whose recommendations are requested
     * @return customer identifier and the matching product recommendations
     */
    @GetMapping("/{user_id}")
    public RecommendationResponse getRecommendations(
            @PathVariable("user_id") UUID userId
    ) {
        return recommendationService.getRecommendations(userId);
    }
}
