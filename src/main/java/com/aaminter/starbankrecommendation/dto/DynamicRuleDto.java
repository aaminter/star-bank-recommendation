package com.aaminter.starbankrecommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Request and response representation of a dynamic recommendation rule.
 * Validation constraints define the required product fields and conditions.
 */
public record DynamicRuleDto(
        Long id,

        @NotBlank(message = "Название продукта (product_name) обязательно")
        @JsonProperty("product_name")
        String productName,

        @NotNull(message = "Идентификатор продукта (product_id) обязателен")
        @JsonProperty("product_id")
        UUID productId,

        @NotBlank(message = "Текст рекомендации (product_text) обязателен")
        @JsonProperty("product_text")
        String productText,

        @NotEmpty(message = "Правило должно содержать хотя бы одно условие")

        List<@Valid RuleQueryDto> rule
) {}
