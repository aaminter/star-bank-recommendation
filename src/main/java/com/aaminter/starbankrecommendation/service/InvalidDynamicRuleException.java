package com.aaminter.starbankrecommendation.service;

/**
 * Signals that a stored dynamic rule cannot be parsed or evaluated as a valid rule.
 */
public class InvalidDynamicRuleException extends RuntimeException {

    /**
     * Creates the exception with a description and the underlying cause.
     *
     * @param message explanation of why the dynamic rule is invalid
     * @param cause underlying parsing or validation failure
     */
    public InvalidDynamicRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}