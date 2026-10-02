package com.digitalheroes.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "stripe_webhook_events",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_stripe_webhook_event_id",
            columnNames = "stripe_event_id"
        )
    }
)
public class StripeWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "stripe_event_id",
        nullable = false,
        unique = true
    )
    private String stripeEventId;

    @Column(
        name = "event_type",
        nullable = false
    )
    private String eventType;

    @Column(
        name = "processed_at",
        nullable = false
    )
    private Instant processedAt;

    public StripeWebhookEvent() {
    }

    public StripeWebhookEvent(
            String stripeEventId,
            String eventType,
            Instant processedAt
    ) {
        this.stripeEventId = stripeEventId;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public String getStripeEventId() {
        return stripeEventId;
    }

    public void setStripeEventId(String stripeEventId) {
        this.stripeEventId = stripeEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}