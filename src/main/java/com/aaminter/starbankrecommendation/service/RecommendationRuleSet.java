package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;

import java.util.Optional;

/**
 * Defines a static rule that may produce a recommendation from customer metrics.
 */
public interface RecommendationRuleSet {

    /**
     * Evaluates this rule for the supplied customer metrics.
     *
     * @param metrics aggregated metrics of the customer
     * @return a recommendation when the rule matches; otherwise an empty optional
     */
    Optional<RecommendationDto> check(CustomerMetrics metrics);
}
