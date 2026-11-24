package com.aaminter.starbankrecommendation.model;

import java.util.UUID;

/**
 * Minimal user data read from the read-only source database for bot replies.
 *
 * @param id source-system customer UUID
 * @param username Telegram or source-system username
 * @param firstName customer's first name
 * @param lastName customer's last name
 */
public record TelegramUser(
        UUID id,
        String username,
        String firstName,
        String lastName
) {
}