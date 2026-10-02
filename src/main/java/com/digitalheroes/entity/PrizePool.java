package com.digitalheroes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "prize_pools",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_draw_tier",
        columnNames = {"draw_id", "tier"}
    )
)
public class PrizePool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draw_id", nullable = false)
    Draw draw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PrizeTier tier;

    @Column(
        name = "allocated_amount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(
        name = "rollover_in",
        nullable = false,
        precision = 12,
        scale = 2
    )
    BigDecimal rolloverIn = BigDecimal.ZERO;

    @Column(
        name = "total_pool",
        nullable = false,
        precision = 12,
        scale = 2
    )
    BigDecimal totalPool = BigDecimal.ZERO;

    @Column(name = "winner_count", nullable = false)
    int winnerCount;

    @Column(
        name = "amount_per_winner",
        precision = 12,
        scale = 2
    )
    BigDecimal amountPerWinner;

    @Column(
        name = "rounding_residual",
        nullable = false,
        precision = 12,
        scale = 2
    )
    BigDecimal roundingResidual = BigDecimal.ZERO;

    @Column(
        name = "rollover_out",
        nullable = false,
        precision = 12,
        scale = 2
    )
    BigDecimal rolloverOut = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    Instant createdAt = Instant.now();

    // No-args constructor
    public PrizePool() {
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Draw getDraw() {
        return draw;
    }

    public PrizeTier getTier() {
        return tier;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public BigDecimal getRolloverIn() {
        return rolloverIn;
    }

    public BigDecimal getTotalPool() {
        return totalPool;
    }

    public int getWinnerCount() {
        return winnerCount;
    }

    public BigDecimal getAmountPerWinner() {
        return amountPerWinner;
    }

    public BigDecimal getRoundingResidual() {
        return roundingResidual;
    }

    public BigDecimal getRolloverOut() {
        return rolloverOut;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setDraw(Draw draw) {
        this.draw = draw;
    }

    public void setTier(PrizeTier tier) {
        this.tier = tier;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public void setRolloverIn(BigDecimal rolloverIn) {
        this.rolloverIn = rolloverIn;
    }

    public void setTotalPool(BigDecimal totalPool) {
        this.totalPool = totalPool;
    }

    public void setWinnerCount(int winnerCount) {
        this.winnerCount = winnerCount;
    }

    public void setAmountPerWinner(BigDecimal amountPerWinner) {
        this.amountPerWinner = amountPerWinner;
    }

    public void setRoundingResidual(BigDecimal roundingResidual) {
        this.roundingResidual = roundingResidual;
    }

    public void setRolloverOut(BigDecimal rolloverOut) {
        this.rolloverOut = rolloverOut;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}