package com.digitalheroes.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record DonationRequest(@NotNull Long charityId,@NotNull @DecimalMin("1.00") BigDecimal amount){}