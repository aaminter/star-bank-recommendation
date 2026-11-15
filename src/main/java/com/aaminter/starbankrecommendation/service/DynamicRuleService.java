package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.model.recommendations.DynamicRule;
import com.aaminter.starbankrecommendation.repository.recommendations.DynamicRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class DynamicRuleService {

    private final DynamicRuleRepository repository;
    private final ObjectMapper objectMapper;

    public DynamicRuleService(DynamicRuleRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional("recommendationsTransactionManager")
    public DynamicRuleDto createRule(DynamicRuleDto dto) {
        try {
            String rulesJson = objectMapper.writeValueAsString(dto.rule());

            DynamicRule entity = new DynamicRule(
                    dto.productId(),
                    dto.productName(),
                    dto.productText(),
                    rulesJson
            );

            DynamicRule saved = repository.save(entity);

            return new DynamicRuleDto(
                    saved.getId(),
                    saved.getProductName(),
                    saved.getProductId(),
                    saved.getProductText(),
                    dto.rule()
            );
        } catch (JacksonException e) {
            throw new RuntimeException("Ошибка при сериализации условий в JSON", e);
        }
    }

    @Transactional(value = "recommendationsTransactionManager", readOnly = true)
    public List<DynamicRuleDto> getAllRules() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional("recommendationsTransactionManager")
    public void deleteRule(Long id) {
        repository.deleteById(id);
    }

    private DynamicRuleDto mapToDto(DynamicRule entity) {
        try {
            List<RuleQueryDto> queries = objectMapper.readValue(
                    entity.getRule(),
                    new TypeReference<>() {}
            );

            return new DynamicRuleDto(
                    entity.getId(),
                    entity.getProductName(),
                    entity.getProductId(),
                    entity.getProductText(),
                    queries
            );
        } catch (JacksonException e) {
            throw new RuntimeException("Ошибка при чтении условий из JSON", e);
        }
    }
}