
package com.digitalheroes.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "charity_id", nullable = false)
    Charity charity;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal amount;

    @Column(nullable = false, length = 3)
    String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DonationStatus status = DonationStatus.PENDING;

    @Column(name = "provider_transaction_id", unique = true)
    String providerTransactionId;

    @Column(name = "created_at", nullable = false)
    Instant createdAt = Instant.now();


    // Getters

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Charity getCharity() {
        return charity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }


    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCharity(Charity charity) {
        this.charity = charity;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }

    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }


    // No-args constructor

    public Donation() {
    }
}

