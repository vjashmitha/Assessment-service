package org.assessment.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}