package com.digitalheroes.repository;

import com.digitalheroes.entity.StripeWebhookEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StripeWebhookEventRepository
        extends JpaRepository<StripeWebhookEvent, Long> {

    Optional<StripeWebhookEvent> findByStripeEventId(
            String stripeEventId
    );

    @Modifying
    @Query(
            value = """
                    INSERT INTO stripe_webhook_events
                        (
                            stripe_event_id,
                            event_type,
                            processed_at
                        )
                    VALUES
                        (
                            :eventId,
                            :eventType,
                            NOW()
                        )
                    ON CONFLICT (stripe_event_id)
                    DO NOTHING
                    """,
            nativeQuery = true
    )
    int claimEvent(
            @Param("eventId") String eventId,
            @Param("eventType") String eventType
    );
}