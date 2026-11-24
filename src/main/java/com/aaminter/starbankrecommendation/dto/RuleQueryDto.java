package com.aaminter.starbankrecommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * One condition in a dynamic rule, including its query arguments and negation flag.
 *
 * @param query supported metric expression to evaluate
 * @param arguments values supplied to the expression
 * @param negate whether the result of the condition must be inverted
 */
public record RuleQueryDto(
        @NotBlank(message = "Поле query не может быть пустым")
        String query,

        @NotEmpty(message = "Список arguments не может быть пустым")
        List<@NotBlank(message = "Аргумент не может быть пустым") String> arguments,

        @NotNull(message = "Поле negate обязательно")
        Boolean negate
) {}