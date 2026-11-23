package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.TelegramUser;

import java.util.List;

/**
 * Formats customer recommendation data as a plain-text Telegram message.
 */
public class TelegramMessageFormatter {

    /**
     * Builds a greeting followed by the supplied product recommendations.
     *
     * @param user customer whose first and last names appear in the greeting
     * @param recommendations recommendations to include in the message
     * @return formatted Telegram message text
     */
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
