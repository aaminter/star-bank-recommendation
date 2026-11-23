package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationResponse;
import com.aaminter.starbankrecommendation.model.TelegramUser;
import com.aaminter.starbankrecommendation.repository.TelegramUserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles incoming Telegram updates for the recommendation bot.
 *
 * <p>Only private chats are processed. Recommendation requests are restricted
 * to explicitly configured Telegram operator IDs.</p>
 */
public class TelegramUpdateHandler implements UpdatesListener {

    private static final String WELCOME_MESSAGE =
            "Здравствуйте! Я бот рекомендаций банковских продуктов.\n"
                    + "Для получения рекомендаций используйте команду:\n"
                    + "/recommend username";

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String ACCESS_DENIED = "Доступ запрещён.";

    private final TelegramBot bot;
    private final TelegramUserRepository userRepository;
    private final RecommendationService recommendationService;
    private final TelegramMessageFormatter messageFormatter;
    private final Set<Long> allowedTelegramUserIds;
    private final Set<Long> welcomedChats = ConcurrentHashMap.newKeySet();

    /**
     * Creates the update handler with the bot, lookup services and access list.
     *
     * @param bot Telegram bot used to send replies
     * @param userRepository repository used to find source-system customers
     * @param recommendationService service generating customer recommendations
     * @param messageFormatter formatter for Telegram recommendation messages
     * @param allowedTelegramUserIds Telegram sender IDs permitted to request recommendations
     */
    public TelegramUpdateHandler(
            TelegramBot bot,
            TelegramUserRepository userRepository,
            RecommendationService recommendationService,
            TelegramMessageFormatter messageFormatter,
            Set<Long> allowedTelegramUserIds
    ) {
        this.bot = bot;
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
        this.messageFormatter = messageFormatter;
        this.allowedTelegramUserIds = Set.copyOf(allowedTelegramUserIds);
    }

    /**
     * Processes a batch of Telegram updates.
     *
     * <p>Malformed updates and non-private chats are ignored. Each private chat
     * receives the welcome message once per handler instance; valid messages
     * are then delegated to the command handler.</p>
     *
     * @param updates batch of updates received from Telegram
     * @return {@link #CONFIRMED_UPDATES_ALL} after the batch has been processed
     */
    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            Message message = update.message();

            if (message == null || message.chat() == null
                    || message.chat().id() == null || message.text() == null) {
                continue;
            }

            // Recommendation data must never be posted to group chats.
            if (message.chat().type() != Chat.Type.Private) {
                continue;
            }

            Long chatId = message.chat().id();

            if (welcomedChats.add(chatId)) {
                sendMessage(chatId, WELCOME_MESSAGE);
            }

            Long senderId = message.from() == null
                    ? null
                    : message.from().id();

            handleMessage(chatId, senderId, message.text().trim());
        }

        return CONFIRMED_UPDATES_ALL;
    }

    private void handleMessage(Long chatId, Long senderId, String text) {
        if (!text.startsWith("/recommend ")) {
            return;
        }

        if (senderId == null || !allowedTelegramUserIds.contains(senderId)) {
            sendMessage(chatId, ACCESS_DENIED);
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
