package com.aaminter.starbankrecommendation.dto;

import java.util.UUID;

public record RecommendationDto(
        String name,
        UUID id,
        String text
) {
}