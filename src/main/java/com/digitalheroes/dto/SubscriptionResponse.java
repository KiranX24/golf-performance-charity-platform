package com.digitalheroes.dto;
import com.digitalheroes.entity.*; import java.time.Instant;
public record SubscriptionResponse(Long id,Long planId,String planName,PlanInterval interval,java.math.BigDecimal price,SubscriptionStatus status,Instant startDate,Instant renewalDate,Instant cancellationDate){}