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
    void formatsUserAndRecommendation() {
        TelegramUser user = new TelegramUser(
                UUID.randomUUID(), "ivan123", "Иван", "Иванов"
        );
        RecommendationDto recommendation = new RecommendationDto(
                "Invest 500", UUID.randomUUID(), "Откройте накопительный счет."
        );

        assertEquals(
                "Здравствуйте Иван Иванов\n\n"
                        + "Новые продукты для вас:"
                        + "\n• Invest 500"
                        + "\n  Откройте накопительный счет.",
                formatter.format(user, List.of(recommendation))
        );
    }

    @Test
    void formatsEmptyRecommendationList() {
        TelegramUser user = new TelegramUser(
                UUID.randomUUID(), "ivan123", "Иван", "Иванов"
        );

        assertEquals(
                "Здравствуйте Иван Иванов\n\nНовые продукты для вас:",
                formatter.format(user, List.of())
        );
    }
}
