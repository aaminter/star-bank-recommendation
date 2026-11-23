package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.dto.RecommendationResponse;
import com.aaminter.starbankrecommendation.model.TelegramUser;
import com.aaminter.starbankrecommendation.repository.TelegramUserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TelegramUpdateHandlerTest {

    private final TelegramBot bot = mock(TelegramBot.class);
    private final TelegramUserRepository userRepository =
            mock(TelegramUserRepository.class);
    private final RecommendationService recommendationService =
            mock(RecommendationService.class);

    private TelegramUpdateHandler handler(Set<Long> allowedIds) {
        return new TelegramUpdateHandler(
                bot,
                userRepository,
                recommendationService,
                new TelegramMessageFormatter(),
                allowedIds
        );
    }

    @Test
    void sendsRecommendationsForAnAuthorizedOperator() {
        UUID userId = UUID.randomUUID();
        TelegramUser user = new TelegramUser(
                userId, "ivan123", "Иван", "Иванов"
        );
        RecommendationDto recommendation = new RecommendationDto(
                "Invest 500", UUID.randomUUID(), "Откройте накопительный счет."
        );

        when(userRepository.findUniqueByUsernameOrFullName("ivan123"))
                .thenReturn(Optional.of(user));
        when(recommendationService.getRecommendations(userId))
                .thenReturn(new RecommendationResponse(userId, List.of(recommendation)));

        handler(Set.of(123L)).process(List.of(createUpdate("/recommend ivan123", 123L)));

        ArgumentCaptor<SendMessage> captor =
                ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(2)).execute(captor.capture());

        assertEquals(
                "Здравствуйте! Я бот рекомендаций банковских продуктов.\n"
                        + "Для получения рекомендаций используйте команду:\n"
                        + "/recommend username",
                captor.getAllValues().get(0).getText()
        );
        assertEquals(
                "Здравствуйте Иван Иванов\n\n"
                        + "Новые продукты для вас:"
                        + "\n• Invest 500"
                        + "\n  Откройте накопительный счет.",
                captor.getAllValues().get(1).getText()
        );
        verify(recommendationService).getRecommendations(userId);
    }

    @Test
    void rejectsUnauthorizedSenderBeforeLookingUpCustomer() {
        handler(Set.of(999L)).process(
                List.of(createUpdate("/recommend ivan123", 123L))
        );

        ArgumentCaptor<SendMessage> captor =
                ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(2)).execute(captor.capture());
        assertEquals("Доступ запрещён.", captor.getAllValues().get(1).getText());
        verifyNoInteractions(userRepository, recommendationService);
    }

    @Test
    void doesNotAuthorizeAnyoneWhenAllowlistIsEmpty() {
        handler(Set.of()).process(
                List.of(createUpdate("/recommend ivan123", 123L))
        );

        verifyNoInteractions(userRepository, recommendationService);
    }

    @Test
    void sendsExactNotFoundMessageWhenLookupIsNotUnique() {
        when(userRepository.findUniqueByUsernameOrFullName("Иван Иванов"))
                .thenReturn(Optional.empty());

        handler(Set.of(123L)).process(
                List.of(createUpdate("/recommend Иван Иванов", 123L))
        );

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
    void welcomesChatOnlyOnceAndIgnoresUnrelatedMessages() {
        TelegramUpdateHandler handler = handler(Set.of(123L));

        handler.process(List.of(createUpdate("/start", 123L)));
        handler.process(List.of(createUpdate("Hello", 123L)));

        verify(bot, times(1)).execute(any(SendMessage.class));
        verifyNoInteractions(userRepository, recommendationService);
    }

    private Update createUpdate(String text, Long senderId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        User sender = mock(User.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(555L);
        when(message.from()).thenReturn(sender);
        when(sender.id()).thenReturn(senderId);
        when(message.text()).thenReturn(text);

        return update;
    }
}
