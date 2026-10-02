package com.digitalheroes.dto;
import jakarta.validation.constraints.NotNull; public record VerificationRequest(@NotNull Boolean approved,String notes){}