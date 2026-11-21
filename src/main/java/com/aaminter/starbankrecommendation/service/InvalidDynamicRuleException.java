package com.aaminter.starbankrecommendation.service;

public class InvalidDynamicRuleException extends RuntimeException {

    public InvalidDynamicRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}