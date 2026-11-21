package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.dto.RecommendationResponse;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.model.DynamicRuleCondition;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecommendationService {

    private static final Logger log =
            LoggerFactory.getLogger(RecommendationService.class);

    private final RecommendationRepository repository;
    private final List<RecommendationRuleSet> ruleSets;
    private final DynamicRuleService dynamicRuleService;
    private final DynamicRuleEngine dynamicRuleEngine;
    private final RuleStatsService ruleStatsService;

    public RecommendationService(
            RecommendationRepository repository,
            List<RecommendationRuleSet> ruleSets,
            DynamicRuleService dynamicRuleService,
            DynamicRuleEngine dynamicRuleEngine,
            RuleStatsService ruleStatsService
    ) {
        this.repository = repository;
        this.ruleSets = ruleSets;
        this.dynamicRuleService = dynamicRuleService;
        this.dynamicRuleEngine = dynamicRuleEngine;
        this.ruleStatsService = ruleStatsService;
    }

    public RecommendationResponse getRecommendations(UUID userId) {
        CustomerMetrics metrics = repository.getCustomerMetrics(userId);

        List<RecommendationDto> recommendations = new ArrayList<>();

        // Сохраняем работу существующих статических правил.
        ruleSets.stream()
                .map(ruleSet -> ruleSet.check(metrics))
                .flatMap(Optional::stream)
                .forEach(recommendations::add);

        // Проверяем динамические правила на том же наборе метрик.
        List<DynamicRuleDto> dynamicRules = dynamicRuleService.getAllRules();

        for (DynamicRuleDto rule : dynamicRules) {
            boolean matches;

            try {
                List<DynamicRuleCondition> conditions = rule.rule().stream()
                        .map(condition -> new DynamicRuleCondition(
                                condition.query(),
                                condition.arguments(),
                                Boolean.TRUE.equals(condition.negate())
                        ))
                        .toList();

                matches = dynamicRuleEngine.matches(metrics, conditions);
            } catch (IllegalArgumentException | NullPointerException e) {
                log.warn(
                        "Skipping invalid dynamic rule with id {}: {}",
                        rule.id(),
                        e.getMessage(),
                        e
                );
                continue;
            }

            if (matches) {
                recommendations.add(new RecommendationDto(
                        rule.productName(),
                        rule.productId(),
                        rule.productText()
                ));

                ruleStatsService.increment(rule.id());
            }
        }

        return new RecommendationResponse(userId, recommendations);
    }
}
