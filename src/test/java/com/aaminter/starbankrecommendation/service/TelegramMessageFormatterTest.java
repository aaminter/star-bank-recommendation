package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.TelegramUser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelegramMessageFormatterTest {

    private final TelegramMessageFormatter formatter =
            new TelegramMessageFormatter();

    @Test
    void shouldFormatUserAndRecommendations() {
        TelegramUser user = new TelegramUser(
                UUID.randomUUID(),
                "ivan123",
                "Иван",
                "Иванов"
        );

        RecommendationDto recommendation = new RecommendationDto(
                "Invest 500",
                UUID.randomUUID(),
                "Откройте накопительный счет."
        );

        String result = formatter.format(user, List.of(recommendation));

        assertEquals(
                "Здравствуйте Иван Иванов\n\n"
                        + "Новые продукты для вас:"
                        + "\n• Invest 500"
                        + "\n  Откройте накопительный счет.",
                result
        );
    }

    @Test
    void shouldFormatUserWithoutRecommendations() {
        TelegramUser user = new TelegramUser(
                UUID.randomUUID(),
                "ivan123",
                "Иван",
                "Иванов"
        );

        String result = formatter.format(user, List.of());

        assertEquals(
                "Здравствуйте Иван Иванов\n\n"
                        + "Новые продукты для вас:",
                result
        );
    }
    @Test
    void shouldFormatMultipleRecommendationsAsSeparateBullets() {
        TelegramUser user = new TelegramUser(
                UUID.randomUUID(), "ivan123", "Ivan", "Petrov"
        );

        RecommendationDto first = new RecommendationDto(
                "Invest 500", UUID.randomUUID(), "First description"
        );
        RecommendationDto second = new RecommendationDto(
                "Save 1000", UUID.randomUUID(), "Second description"
        );

        String result = formatter.format(user, List.of(first, second));

        assertEquals(
                "Здравствуйте Ivan Petrov\n\n"
                        + "Новые продукты для вас:"
                        + "\n• Invest 500"
                        + "\n  First description"
                        + "\n• Save 1000"
                        + "\n  Second description",
                result
        );
    }
}