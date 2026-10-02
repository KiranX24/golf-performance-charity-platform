package com.digitalheroes.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response shape used across the whole API (PRD §23).
 *
 *   {
 *     "success": false,
 *     "message": "Human-readable message",
 *     "timestamp": "...",
 *     "path": "...",
 *     "errors": []
 *   }
 */
public record ApiErrorResponse(
    boolean success,
    String message,
    Instant timestamp,
    String path,
    List<String> errors
) {
    public static ApiErrorResponse of(String message, String path, List<String> errors) {
        return new ApiErrorResponse(false, message, Instant.now(), path, errors);
    }
}
