package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.TelegramUser;

import java.util.List;

public class TelegramMessageFormatter {

    public String format(
            TelegramUser user,
            List<RecommendationDto> recommendations
    ) {
        StringBuilder message = new StringBuilder()
                .append("Здравствуйте ")
                .append(user.firstName())
                .append(" ")
                .append(user.lastName())
                .append("\n\n")
                .append("Новые продукты для вас:");

        for (RecommendationDto recommendation : recommendations) {
            message.append("\n• ")
                    .append(recommendation.name())
                    .append("\n  ")
                    .append(recommendation.text());
        }

        return message.toString();
    }
}