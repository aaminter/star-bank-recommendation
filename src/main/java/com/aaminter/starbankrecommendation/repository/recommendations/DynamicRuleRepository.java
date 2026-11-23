package com.aaminter.starbankrecommendation.repository.recommendations;

import com.aaminter.starbankrecommendation.model.recommendations.DynamicRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides CRUD persistence for dynamic recommendation rules.
 *
 * <p>The entity identifier is a database-generated {@link Long} value,
 * corresponding to PostgreSQL BIGINT.</p>
 */
@Repository
public interface DynamicRuleRepository extends JpaRepository<DynamicRule, Long> {

}
