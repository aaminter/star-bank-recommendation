package com.aaminter.starbankrecommendation.dto;

import java.util.UUID;

/**
 * A single product recommendation returned by the recommendation API.
 *
 * @param name product display name
 * @param id product identifier
 * @param text recommendation description
 */
public record RecommendationDto(
        String name,
        UUID id,
        String text
) {
}