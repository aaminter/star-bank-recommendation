package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.model.recommendations.DynamicRule;
import com.aaminter.starbankrecommendation.repository.recommendations.DynamicRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates, retrieves and deletes dynamic recommendation rules stored in PostgreSQL.
 *
 * <p>Rule conditions are persisted as JSON. Invalid stored JSON is skipped when
 * rules are retrieved, so one malformed record does not prevent other rules
 * from being returned.</p>
 */
@Service
public class DynamicRuleService {

    private static final Logger log =
            LoggerFactory.getLogger(DynamicRuleService.class);

    private final DynamicRuleRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Creates the service with the dynamic rule repository and JSON mapper.
     *
     * @param repository repository for persisted dynamic rules
     * @param objectMapper mapper used to serialize and deserialize rule conditions
     */
    public DynamicRuleService(
            DynamicRuleRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Validates and persists a dynamic rule.
     *
     * @param dto rule data, including product information and conditions
     * @return the persisted rule with its generated database identifier
     * @throws IllegalArgumentException if a condition type or its arguments are invalid
     */
    @Transactional("recommendationsTransactionManager")
    public DynamicRuleDto createRule(DynamicRuleDto dto) {
        // Reject unsupported queries and malformed arguments before persistence.
        DynamicRuleValidator.validate(dto.rule());

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
            throw new RuntimeException(
                    "Ошибка при сериализации условий в JSON", e
            );
        }
    }

    /**
     * Loads all stored rules and converts valid records to DTOs.
     *
     * <p>Records whose condition JSON cannot be deserialized are logged and
     * omitted from the returned list.</p>
     *
     * @return all successfully deserialized dynamic rules
     */
    @Transactional(value = "recommendationsTransactionManager", readOnly = true)
    public List<DynamicRuleDto> getAllRules() {
        List<DynamicRuleDto> rules = new ArrayList<>();

        for (DynamicRule entity : repository.findAll()) {
            try {
                rules.add(mapToDto(entity));
            } catch (InvalidDynamicRuleException e) {
                log.warn(
                        "Skipping invalid dynamic rule with id {}: {}",
                        entity.getId(),
                        e.getMessage()
                );
            }
        }

        return rules;
    }

    /**
     * Deletes a dynamic rule by its database identifier.
     *
     * @param id Long identifier of the dynamic rule record
     * @throws EntityNotFoundException if no rule with the identifier exists
     */
    @Transactional("recommendationsTransactionManager")
    public void deleteRule(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Правило с id " + id + " не найдено"
            );
        }

        repository.deleteById(id);
    }

    /**
     * Converts a persisted entity to an API DTO by deserializing its JSON conditions.
     *
     * @param entity persisted dynamic rule
     * @return DTO containing the entity fields and deserialized conditions
     * @throws InvalidDynamicRuleException if the stored condition JSON cannot be deserialized
     */
    private DynamicRuleDto mapToDto(DynamicRule entity) {
        try {
            List<RuleQueryDto> queries = objectMapper.readValue(
                    entity.getRule(),
                    new TypeReference<>() {
                    }
            );

            return new DynamicRuleDto(
                    entity.getId(),
                    entity.getProductName(),
                    entity.getProductId(),
                    entity.getProductText(),
                    queries
            );
        } catch (JacksonException e) {
            throw new InvalidDynamicRuleException(
                    "Cannot read conditions from JSON", e
            );
        }
    }
}
