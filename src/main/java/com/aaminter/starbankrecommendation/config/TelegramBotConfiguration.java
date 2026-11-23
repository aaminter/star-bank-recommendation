package com.aaminter.starbankrecommendation.config;

import com.aaminter.starbankrecommendation.repository.TelegramUserRepository;
import com.aaminter.starbankrecommendation.service.RecommendationService;
import com.aaminter.starbankrecommendation.service.TelegramMessageFormatter;
import com.aaminter.starbankrecommendation.service.TelegramUpdateHandler;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(name = "telegram.bot.token")
public class TelegramBotConfiguration {

    @Bean(destroyMethod = "shutdown")
    public TelegramBot telegramBot(
            @Value("${telegram.bot.token}") String token,
            @Value("${telegram.bot.allowed-user-ids:}") String allowedUserIds,
            TelegramUserRepository userRepository,
            RecommendationService recommendationService
    ) {
        TelegramBot bot = new TelegramBot(token);

        Set<Long> allowedIds = Arrays.stream(allowedUserIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toUnmodifiableSet());

        TelegramUpdateHandler handler = new TelegramUpdateHandler(
                bot,
                userRepository,
                recommendationService,
                new TelegramMessageFormatter(),
                allowedIds
        );

        bot.setUpdatesListener(handler);

        return bot;
    }
}
