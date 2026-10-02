package com.digitalheroes.dto;

public record CharityResponse(
        Long id,
        String name,
        String slug,
        String description,
        String logoUrl,
        boolean featured,
        boolean archived
) {
}