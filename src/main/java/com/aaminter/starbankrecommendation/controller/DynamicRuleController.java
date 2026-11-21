package com.aaminter.starbankrecommendation.controller;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.service.DynamicRuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/rule")
public class DynamicRuleController {
    private final DynamicRuleService ruleService;

    public DynamicRuleController(DynamicRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    public ResponseEntity<DynamicRuleDto> createRule(@Valid @RequestBody DynamicRuleDto ruleDto) {
        DynamicRuleDto created = ruleService.createRule(ruleDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<DynamicRuleDto>>> getAllRules() {
        List<DynamicRuleDto> rules = ruleService.getAllRules();
        return ResponseEntity.ok(Map.of("data", rules));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}