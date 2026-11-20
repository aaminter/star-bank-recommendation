package com.aaminter.starbankrecommendation.repository.recommendations;

import com.aaminter.starbankrecommendation.model.recommendations.DynamicRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynamicRuleRepository extends JpaRepository<DynamicRule, Long> {

}