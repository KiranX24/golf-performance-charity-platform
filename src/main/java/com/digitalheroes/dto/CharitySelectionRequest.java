package com.digitalheroes.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CharitySelectionRequest(@NotNull Long charityId,
		@NotNull @DecimalMin("10.00") @DecimalMax("100.00") BigDecimal contributionPct) {
}