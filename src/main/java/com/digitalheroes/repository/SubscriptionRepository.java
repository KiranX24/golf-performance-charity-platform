package com.digitalheroes.repository;

import com.digitalheroes.entity.Subscription;
import com.digitalheroes.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByUserIdAndStatus(
            Long userId,
            SubscriptionStatus status
    );

    List<Subscription> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<Subscription> findByStripeSubscriptionId(
            String stripeSubscriptionId
    );
}