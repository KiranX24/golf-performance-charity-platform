package com.digitalheroes.dto;
import com.digitalheroes.entity.PlanInterval; import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record AdminPlanRequest(@NotBlank String name,@NotNull PlanInterval interval,@NotNull @DecimalMin("0.00") BigDecimal price,@NotBlank String currency,boolean active,String stripePriceId){}