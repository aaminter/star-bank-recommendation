package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.dto.RecommendationResponse;
import com.aaminter.starbankrecommendation.model.TelegramUser;
import com.aaminter.starbankrecommendation.repository.TelegramUserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TelegramUpdateHandlerTest {

    private final TelegramBot bot = mock(TelegramBot.class);
    private final TelegramUserRepository userRepository =
            mock(TelegramUserRepository.class);
    private final RecommendationService recommendationService =
            mock(RecommendationService.class);

    private final TelegramUpdateHandler handler = new TelegramUpdateHandler(
            bot,
            userRepository,
            recommendationService,
            new TelegramMessageFormatter()
    );

    @Test
    void shouldSendWelcomeAndRecommendationsForFullName() {
        UUID userId = UUID.randomUUID();
        TelegramUser user = new TelegramUser(
                userId, "ivan123", "Иван", "Иванов"
        );

        RecommendationDto recommendation = new RecommendationDto(
                "Invest 500",
                UUID.randomUUID(),
                "Откройте накопительный счет."
        );

        when(userRepository.findUniqueByUsernameOrFullName("Иван Иванов"))
                .thenReturn(Optional.of(user));

        when(recommendationService.getRecommendations(userId))
                .thenReturn(new RecommendationResponse(
                        userId, List.of(recommendation)
                ));

        handler.process(List.of(createUpdate("/recommend Иван Иванов")));

        ArgumentCaptor<SendMessage> captor =
                ArgumentCaptor.forClass(SendMessage.class);

        verify(bot, times(2)).execute(captor.capture());

        List<SendMessage> messages = captor.getAllValues();

        assertEquals(
                "Здравствуйте! Я бот рекомендаций банковских продуктов.\n"
                        + "Для получения рекомендаций используйте команду:\n"
                        + "/recommend username",
                messages.get(0).getText()
        );

        assertEquals(
                "Здравствуйте Иван Иванов\n\n"
                        + "Новые продукты для вас:"
                        + "\n• Invest 500"
                        + "\n  Откройте накопительный счет.",
                messages.get(1).getText()
        );

        verify(userRepository)
                .findUniqueByUsernameOrFullName("Иван Иванов");
        verify(recommendationService).getRecommendations(userId);
    }

    @Test
    void shouldSendNotFoundWhenUserDoesNotExist() {
        when(userRepository.findUniqueByUsernameOrFullName("unknown"))
                .thenReturn(Optional.empty());

        handler.process(List.of(createUpdate("/recommend unknown")));

        ArgumentCaptor<SendMessage> captor =
                ArgumentCaptor.forClass(SendMessage.class);

        verify(bot, times(2)).execute(captor.capture());

        assertEquals(
                "Пользователь не найден",
                captor.getAllValues().get(1).getText()
        );

        verifyNoInteractions(recommendationService);
    }

    @Test
    void shouldFindUserByUsernameAndWelcomeOnlyOncePerChat() {
        UUID userId = UUID.randomUUID();
        TelegramUser user = new TelegramUser(
                userId, "ivan123", "Иван", "Иванов"
        );

        when(userRepository.findUniqueByUsernameOrFullName("ivan123"))
                .thenReturn(Optional.of(user));
        when(userRepository.findUniqueByUsernameOrFullName("IVAN123"))
                .thenReturn(Optional.of(user));
        when(recommendationService.getRecommendations(userId))
                .thenReturn(new RecommendationResponse(userId, List.of()));

        handler.process(List.of(createUpdate("/recommend ivan123")));
        handler.process(List.of(createUpdate("/recommend IVAN123")));

        ArgumentCaptor<SendMessage> captor =
                ArgumentCaptor.forClass(SendMessage.class);

        verify(bot, times(3)).execute(captor.capture());

        List<SendMessage> messages = captor.getAllValues();

        assertEquals(
                "Здравствуйте! Я бот рекомендаций банковских продуктов.\n"
                        + "Для получения рекомендаций используйте команду:\n"
                        + "/recommend username",
                messages.get(0).getText()
        );

        assertEquals(
                "Здравствуйте Иван Иванов\n\nНовые продукты для вас:",
                messages.get(1).getText()
        );
        assertEquals(messages.get(1).getText(), messages.get(2).getText());

        verify(userRepository)
                .findUniqueByUsernameOrFullName("ivan123");
        verify(userRepository)
                .findUniqueByUsernameOrFullName("IVAN123");
        verify(recommendationService, times(2)).getRecommendations(userId);
    }
    @Test
    void shouldIgnoreUnrelatedMessages() {
        handler.process(List.of(createUpdate("/start")));
        handler.process(List.of(createUpdate("Hello")));

        verify(bot, times(1)).execute(any(SendMessage.class));
        verifyNoInteractions(userRepository, recommendationService);
    }

    @Test
    void shouldIgnoreRecommendCommandWithoutArgument() {
        handler.process(List.of(createUpdate("/recommend")));

        verify(bot, times(1)).execute(any(SendMessage.class));
        verifyNoInteractions(userRepository, recommendationService);
    }
    private Update createUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
        when(message.text()).thenReturn(text);

        return update;
    }
}