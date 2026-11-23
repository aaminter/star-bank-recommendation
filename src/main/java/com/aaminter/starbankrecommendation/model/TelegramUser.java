package com.aaminter.starbankrecommendation.model;

import java.util.UUID;

/**
 * Minimal user data read from the read-only source database for bot replies.
 */
public record TelegramUser(
        UUID id,
        String username,
        String firstName,
        String lastName
) {
}
