package com.digitalheroes.dto;
import jakarta.validation.constraints.*; import java.time.LocalDate;
public record ScoreRequest(@Min(1) @Max(45) short scoreValue,@NotNull LocalDate scoreDate){}