package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;

import java.util.Optional;

public interface RecommendationRuleSet {

    Optional<RecommendationDto> check(CustomerMetrics metrics);
}
