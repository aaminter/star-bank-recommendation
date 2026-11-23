package com.aaminter.starbankrecommendation.model;

import java.util.UUID;

public record TelegramUser(
        UUID id,
        String username,
        String firstName,
        String lastName
) {
}
