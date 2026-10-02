package com.digitalheroes.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "winners",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_draw_user_tier",
        columnNames = {"draw_id", "user_id", "tier"}
    )
)
public class Winner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draw_id", nullable = false)
    Draw draw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PrizeTier tier;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    VerificationStatus verificationStatus =
            VerificationStatus.PENDING_VERIFICATION;

    @Column(name = "verification_notes")
    String verificationNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    User verifiedBy;

    @Column(name = "verified_at")
    Instant verifiedAt;

    @Column(name = "created_at", nullable = false)
    Instant createdAt = Instant.now();


    // Getters

    public Long getId() {
        return id;
    }

    public Draw getDraw() {
        return draw;
    }

    public User getUser() {
        return user;
    }

    public PrizeTier getTier() {
        return tier;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
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

    public void setUser(User user) {
        this.user = user;
    }

    public void setTier(PrizeTier tier) {
        this.tier = tier;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }


    // No-args constructor

    public Winner() {
    }
}

