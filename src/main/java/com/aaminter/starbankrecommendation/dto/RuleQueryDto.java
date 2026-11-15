package com.aaminter.starbankrecommendation.dto;

import java.util.List;

public record RuleQueryDto(
        String query,
        List<String> arguments,
        boolean negate
) {
}