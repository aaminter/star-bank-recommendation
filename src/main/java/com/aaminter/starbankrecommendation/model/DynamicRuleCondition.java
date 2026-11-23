package com.aaminter.starbankrecommendation.model;

import java.util.List;
import java.util.Objects;

/**
 * Immutable condition used by the dynamic rule evaluation engine.
 */
public record DynamicRuleCondition(
        String query,
        List<String> arguments,
        boolean negate
) {
    public DynamicRuleCondition {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Query must not be empty"
            );
        }

        Objects.requireNonNull(
                arguments,
                "Arguments must not be null"
        );

        arguments = List.copyOf(arguments);
    }
}