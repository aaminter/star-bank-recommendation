package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.repository.recommendations.RuleStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class RuleStatsService {

    private final RuleStatsRepository repository;

    public RuleStatsService(RuleStatsRepository repository) {
        this.repository = repository;
    }

    @Transactional("recommendationsTransactionManager")
    public void increment(Long ruleId) {
        repository.increment(ruleId);
    }

    @Transactional(
            value = "recommendationsTransactionManager",
            readOnly = true
    )
    public List<Map<String, String>> getAllStats() {
        return repository.findAllStats().stream()
                .map(row -> Map.of(
                        "rule_id", String.valueOf(row[0]),
                        "count", String.valueOf(row[1])
                ))
                .toList();
    }
}