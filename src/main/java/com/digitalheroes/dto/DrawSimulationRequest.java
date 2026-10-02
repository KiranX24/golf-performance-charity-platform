package com.digitalheroes.dto;
import com.digitalheroes.entity.ScoreMode; import jakarta.validation.constraints.NotNull; import java.time.LocalDate;
public record DrawSimulationRequest(@NotNull LocalDate drawPeriod,@NotNull ScoreMode mode){}