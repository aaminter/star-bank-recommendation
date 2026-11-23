
package com.aaminter.starbankrecommendation.controller;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.service.DynamicRuleService;
import com.aaminter.starbankrecommendation.service.RuleStatsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rule")
public class DynamicRuleController {

    private final DynamicRuleService ruleService;
    private final RuleStatsService ruleStatsService;

    public DynamicRuleController(
            DynamicRuleService ruleService,
            RuleStatsService ruleStatsService
    ) {
        this.ruleService = ruleService;
        this.ruleStatsService = ruleStatsService;
    }

    @PostMapping
    public ResponseEntity<DynamicRuleDto> createRule(
            @Valid @RequestBody DynamicRuleDto ruleDto
    ) {
        DynamicRuleDto created = ruleService.createRule(ruleDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<DynamicRuleDto>>> getAllRules() {
        List<DynamicRuleDto> rules = ruleService.getAllRules();
        return ResponseEntity.ok(Map.of("data", rules));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, List<Map<String, String>>>> getRuleStats() {
        List<Map<String, String>> stats = ruleStatsService.getAllStats();
        return ResponseEntity.ok(Map.of("stats", stats));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
