package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.dto.RecommendationResponse;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecommendationService {

    private final RecommendationRepository repository;
    private final List<RecommendationRuleSet> ruleSets;

    public RecommendationService(
            RecommendationRepository repository,
            List<RecommendationRuleSet> ruleSets
    ) {
        this.repository = repository;
        this.ruleSets = ruleSets;
    }

    public RecommendationResponse getRecommendations(UUID userId) {
        CustomerMetrics metrics = repository.getCustomerMetrics(userId);

        List<RecommendationDto> recommendations = ruleSets.stream()
                .map(ruleSet -> ruleSet.check(metrics))
                .flatMap(Optional::stream)
                .toList();

        return new RecommendationResponse(userId, recommendations);
    }
}
