package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationResponse;
import com.aaminter.starbankrecommendation.model.TelegramUser;
import com.aaminter.starbankrecommendation.repository.TelegramUserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramUpdateHandler implements UpdatesListener {

    private static final String WELCOME_MESSAGE =
            "Здравствуйте! Я бот рекомендаций банковских продуктов.\n"
                    + "Для получения рекомендаций используйте команду:\n"
                    + "/recommend username";

    private static final String USER_NOT_FOUND = "Пользователь не найден";

    private final TelegramBot bot;
    private final TelegramUserRepository userRepository;
    private final RecommendationService recommendationService;
    private final TelegramMessageFormatter messageFormatter;
    private final Set<Long> welcomedChats = ConcurrentHashMap.newKeySet();

    public TelegramUpdateHandler(
            TelegramBot bot,
            TelegramUserRepository userRepository,
            RecommendationService recommendationService,
            TelegramMessageFormatter messageFormatter
    ) {
        this.bot = bot;
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            Message message = update.message();

            if (message == null || message.chat() == null
                    || message.chat().id() == null || message.text() == null) {
                continue;
            }

            Long chatId = message.chat().id();

            if (welcomedChats.add(chatId)) {
                sendMessage(chatId, WELCOME_MESSAGE);
            }

            handleMessage(chatId, message.text().trim());
        }

        return CONFIRMED_UPDATES_ALL;
    }

    private void handleMessage(Long chatId, String text) {
        if (!text.startsWith("/recommend ")) {
            return;
        }

        String searchValue = text.substring("/recommend ".length()).trim();

        if (searchValue.isEmpty()) {
            return;
        }

        Optional<TelegramUser> user =
                userRepository.findUniqueByUsernameOrFullName(searchValue);

        if (user.isEmpty()) {
            sendMessage(chatId, USER_NOT_FOUND);
            return;
        }

        RecommendationResponse response =
                recommendationService.getRecommendations(user.get().id());

        sendMessage(
                chatId,
                messageFormatter.format(user.get(), response.recommendations())
        );
    }

    private void sendMessage(Long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }
}