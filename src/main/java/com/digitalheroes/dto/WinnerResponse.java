package com.digitalheroes.dto;

import com.digitalheroes.entity.PrizeTier;
import com.digitalheroes.entity.PayoutStatus;
import com.digitalheroes.entity.VerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record WinnerResponse(
        Long id,
        Long drawId,
        Long userId,
        String userName,
        PrizeTier tier,
        BigDecimal amount,
        VerificationStatus verificationStatus,

        Long payoutId,
        PayoutStatus payoutStatus,
        Instant paidAt
) {
}