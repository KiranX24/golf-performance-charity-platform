package com.digitalheroes.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    Instant startDate;

    @Column(name = "renewal_date")
    Instant renewalDate;

    @Column(name = "cancellation_date")
    Instant cancellationDate;

    @Column(name = "stripe_subscription_id", unique = true)
    String stripeSubscriptionId;

    @Column(name = "stripe_customer_id")
    String stripeCustomerId;

    @Column(name = "created_at", nullable = false)
    Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();


    // Getters

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getRenewalDate() {
        return renewalDate;
    }

    public Instant getCancellationDate() {
        return cancellationDate;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }


    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public void setRenewalDate(Instant renewalDate) {
        this.renewalDate = renewalDate;
    }

    public void setCancellationDate(Instant cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }


    // No-args constructor

    public Subscription() {
    }
}

