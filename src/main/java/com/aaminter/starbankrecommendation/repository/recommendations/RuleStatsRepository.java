package com.aaminter.starbankrecommendation.repository.recommendations;

import com.aaminter.starbankrecommendation.model.recommendations.RuleStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Persists and queries execution statistics for dynamic recommendation rules.
 *
 * <p>Statistics are keyed by the dynamic rule's database identifier
 * (Long/BIGINT), not by the product UUID.</p>
 */
@Repository
public interface RuleStatsRepository extends JpaRepository<RuleStats, Long> {

    /**
     * Atomically creates a counter or increments the existing counter by one.
     *
     * @param ruleId database identifier of the dynamic rule
     */
    @Modifying
    @Query(value = """
            INSERT INTO dynamic_rule_stats (rule_id, count)
            VALUES (:ruleId, 1)
            ON CONFLICT (rule_id)
            DO UPDATE SET count = dynamic_rule_stats.count + 1
            """, nativeQuery = true)
    void increment(@Param("ruleId") Long ruleId);

    /**
     * Returns a counter row for every dynamic rule, including rules not yet executed.
     *
     * <p>The left join preserves rules without a statistics record and
     * {@code COALESCE} reports their count as zero.</p>
     *
     * @return rows containing the rule database identifier and its counter
     */
    @Query(value = """
            SELECT r.id AS rule_id,
                   COALESCE(s.count, 0) AS count
            FROM dynamic_rules r
            LEFT JOIN dynamic_rule_stats s ON s.rule_id = r.id
            ORDER BY r.id
            """, nativeQuery = true)
    List<Object[]> findAllStats();
}
