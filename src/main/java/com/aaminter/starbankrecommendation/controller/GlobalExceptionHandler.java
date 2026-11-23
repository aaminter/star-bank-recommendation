package com.aaminter.starbankrecommendation.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps common request and domain exceptions to consistent REST error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converts bean validation failures into a {@code 400 Bad Request} response.
     *
     * @param ex validation exception containing field-level errors
     * @return response with status, error description and field details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Validation Failed",
                "details", fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Converts unreadable request bodies, including malformed JSON, into a {@code 400} response.
     *
     * @param ex exception raised while reading the request body
     * @return response describing the malformed request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableJson(HttpMessageNotReadableException ex) {
        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Malformed JSON request",
                "details", "Проверьте корректность синтаксиса JSON и форматы полей (например, UUID)"
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Converts invalid rule definitions and arguments into a {@code 400} response.
     *
     * @param ex exception describing the invalid argument or rule
     * @return response containing the validation error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRule(IllegalArgumentException ex) {
        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Invalid rule",
                "details", ex.getMessage() != null
                        ? ex.getMessage()
                        : "Проверьте тип условия и его аргументы"
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Converts missing database entities into a {@code 404 Not Found} response.
     *
     * @param ex exception describing the entity that could not be found
     * @return response containing the not-found status and details
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Запись не найдена";
        Map<String, Object> response = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Not Found",
                "details", message
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
