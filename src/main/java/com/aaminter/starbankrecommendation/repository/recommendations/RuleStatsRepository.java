package com.aaminter.starbankrecommendation.repository.recommendations;

import com.aaminter.starbankrecommendation.model.recommendations.RuleStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleStatsRepository extends JpaRepository<RuleStats, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO dynamic_rule_stats (rule_id, count)
            VALUES (:ruleId, 1)
            ON CONFLICT (rule_id)
            DO UPDATE SET count = dynamic_rule_stats.count + 1
            """, nativeQuery = true)
    void increment(@Param("ruleId") Long ruleId);

    @Query(value = """
            SELECT r.id AS rule_id,
                   COALESCE(s.count, 0) AS count
            FROM dynamic_rules r
            LEFT JOIN dynamic_rule_stats s ON s.rule_id = r.id
            ORDER BY r.id
            """, nativeQuery = true)
    List<Object[]> findAllStats();
}