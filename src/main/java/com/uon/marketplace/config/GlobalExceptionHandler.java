package com.uon.marketplace.config;

import com.uon.marketplace.exceptions.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

// Temporarily disabled due to springdoc-openapi compatibility issue with Spring 6.2
// TODO: Re-enable when springdoc is updated or downgrade Spring
// @ControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("reason", "email_already_registered");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
