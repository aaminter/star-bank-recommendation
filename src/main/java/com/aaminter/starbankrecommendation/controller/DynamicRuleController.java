package com.aaminter.starbankrecommendation.controller;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.service.DynamicRuleService;
import com.aaminter.starbankrecommendation.service.RuleStatsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for managing dynamic recommendation rules and their statistics.
 */
@RestController
@RequestMapping("/rule")
public class DynamicRuleController {

    private final DynamicRuleService ruleService;
    private final RuleStatsService ruleStatsService;

    /**
     * Creates the controller with the rule and statistics services.
     *
     * @param ruleService service for dynamic rule operations
     * @param ruleStatsService service for retrieving rule execution statistics
     */
    public DynamicRuleController(
            DynamicRuleService ruleService,
            RuleStatsService ruleStatsService
    ) {
        this.ruleService = ruleService;
        this.ruleStatsService = ruleStatsService;
    }

    /**
     * Validates and creates a dynamic rule.
     *
     * @param ruleDto rule definition received in the request body
     * @return created rule with its database identifier
     */
    @PostMapping
    public ResponseEntity<DynamicRuleDto> createRule(
            @Valid @RequestBody DynamicRuleDto ruleDto
    ) {
        DynamicRuleDto created = ruleService.createRule(ruleDto);
        return ResponseEntity.ok(created);
    }

    /**
     * Returns all valid dynamic rules.
     *
     * @return response containing the rules under the {@code data} key
     */
    @GetMapping
    public ResponseEntity<Map<String, List<DynamicRuleDto>>> getAllRules() {
        List<DynamicRuleDto> rules = ruleService.getAllRules();
        return ResponseEntity.ok(Map.of("data", rules));
    }

    /**
     * Returns execution counters for all stored dynamic rules.
     *
     * @return response containing {@code rule_id} and {@code count} string values
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, List<Map<String, String>>>> getRuleStats() {
        List<Map<String, String>> stats = ruleStatsService.getAllStats();
        return ResponseEntity.ok(Map.of("stats", stats));
    }

    /**
     * Deletes a dynamic rule by its database identifier.
     *
     * @param id Long identifier of the dynamic rule
     * @return {@code 204 No Content} when deletion succeeds
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
