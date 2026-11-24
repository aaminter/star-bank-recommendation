package com.aaminter.starbankrecommendation.model.recommendations;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity storing the number of times a dynamic rule has been triggered.
 */
@Entity
@Table(name = "dynamic_rule_stats")
public class RuleStats {

    @Id
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "count", nullable = false)
    private Long count;

    protected RuleStats() {
    }

    /**
     * Creates a statistics record for a dynamic rule.
     *
     * @param ruleId dynamic rule identifier
     * @param count number of recorded triggers
     */
    public RuleStats(Long ruleId, Long count) {
        this.ruleId = ruleId;
        this.count = count;
    }

    /**
     * Returns the identifier of the rule whose executions are counted.
     *
     * @return dynamic rule identifier
     */
    public Long getRuleId() {
        return ruleId;
    }

    /**
     * Returns the number of times the rule has been triggered.
     *
     * @return execution count
     */
    public Long getCount() {
        return count;
    }
}