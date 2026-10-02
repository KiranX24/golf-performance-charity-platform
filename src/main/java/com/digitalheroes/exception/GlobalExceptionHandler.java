package com.digitalheroes.exception;

import com.digitalheroes.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(
            MethodArgumentNotValidException e,
            HttpServletRequest r
    ) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        "Validation failed",
                        r.getRequestURI(),
                        e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(x -> x.getField() + ": " + x.getDefaultMessage())
                                .toList()
                ));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    ResponseEntity<ApiErrorResponse> business(
            RuntimeException e,
            HttpServletRequest r
    ) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        e.getMessage(),
                        r.getRequestURI(),
                        List.of()
                ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(
            Exception e,
            HttpServletRequest r
    ) {

        // Temporary debugging
        e.printStackTrace();

        String message = e.getMessage();

        if (message == null || message.isBlank()) {
            message = e.getClass().getSimpleName();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        message,
                        r.getRequestURI(),
                        List.of()
                ));
    }
}