package com.aaminter.starbankrecommendation.dto;

import java.util.UUID;

/**
 * A single product recommendation returned by the recommendation API.
 */
/**
 * A single product recommendation returned by the recommendation API.
 */
public record RecommendationDto(
        String name,
        UUID id,
        String text
) {
}