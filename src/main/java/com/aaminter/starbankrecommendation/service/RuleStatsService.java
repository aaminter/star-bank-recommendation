package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.repository.recommendations.RuleStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Records and retrieves execution counters for dynamic recommendation rules.
 *
 * <p>Rule identifiers are database-generated Long values (PostgreSQL BIGINT),
 * distinct from the UUID product identifiers carried by recommendations.</p>
 */
@Service
public class RuleStatsService {

    private final RuleStatsRepository repository;

    /**
     * Creates the service with the rule statistics repository.
     *
     * @param repository repository that updates and reads rule counters
     */
    public RuleStatsService(RuleStatsRepository repository) {
        this.repository = repository;
    }

    /**
     * Atomically increments the execution counter for a dynamic rule.
     *
     * <p>If the counter row does not yet exist, the repository creates it
     * with a value of one.</p>
     *
     * @param ruleId Long database identifier of the dynamic rule
     */
    @Transactional("recommendationsTransactionManager")
    public void increment(Long ruleId) {
        repository.increment(ruleId);
    }

    /**
     * Retrieves counters for all stored dynamic rules.
     *
     * <p>Rules without a counter row are included with a count of zero.
     * Both {@code rule_id} and {@code count} are returned as decimal strings.</p>
     *
     * @return statistics rows containing {@code rule_id} and {@code count}
     */
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