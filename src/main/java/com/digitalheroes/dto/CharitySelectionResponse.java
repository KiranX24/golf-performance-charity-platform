package com.digitalheroes.dto;
import java.math.BigDecimal; import java.time.Instant;
public record CharitySelectionResponse(Long charityId,String charityName,BigDecimal contributionPct,Instant effectiveFrom){}