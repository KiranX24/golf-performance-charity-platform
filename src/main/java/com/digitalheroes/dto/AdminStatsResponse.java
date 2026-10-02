package com.digitalheroes.dto;

public record AdminStatsResponse(long users, long activeSubscriptions, long scores, long charities, long draws,
		long winners, long pendingVerifications) {
}