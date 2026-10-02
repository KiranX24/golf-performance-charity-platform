package com.digitalheroes.dto;

import com.digitalheroes.entity.PlanInterval;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String name,
        PlanInterval interval,
        BigDecimal price,
        String currency,
        boolean active
) {
}