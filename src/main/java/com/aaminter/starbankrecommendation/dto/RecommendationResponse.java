package com.aaminter.starbankrecommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * API response containing the requested user's ID and their recommendations.
 *
 * @param userId identifier of the customer for whom recommendations were generated
 * @param recommendations products recommended to the customer
 */
public record RecommendationResponse(
        @JsonProperty("user_id")
        UUID userId,

        List<RecommendationDto> recommendations
) {
}