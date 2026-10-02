package com.digitalheroes.service;

import com.digitalheroes.entity.StripeWebhookEvent;
import com.digitalheroes.entity.Subscription;
import com.digitalheroes.entity.SubscriptionPlan;
import com.digitalheroes.entity.SubscriptionStatus;
import com.digitalheroes.entity.User;

import com.digitalheroes.repository.StripeWebhookEventRepository;
import com.digitalheroes.repository.SubscriptionPlanRepository;
import com.digitalheroes.repository.SubscriptionRepository;
import com.digitalheroes.repository.UserRepository;

import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceLineItem;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StripeWebhookService {

    private final UserRepository users;
    private final SubscriptionRepository subscriptions;
    private final SubscriptionPlanRepository plans;
    private final StripeWebhookEventRepository webhookEvents;
    private final String stripeSecretKey;

    public StripeWebhookService(
            UserRepository users,
            SubscriptionRepository subscriptions,
            SubscriptionPlanRepository plans,
            StripeWebhookEventRepository webhookEvents,
            @Value("${app.stripe.secret-key}") String stripeSecretKey
    ) {
        this.users = users;
        this.subscriptions = subscriptions;
        this.plans = plans;
        this.webhookEvents = webhookEvents;
        this.stripeSecretKey = stripeSecretKey;
    }

    // =========================================================
    // EVENT IDEMPOTENCY
    // =========================================================

    @Transactional
    public boolean alreadyProcessed(String eventId) {

        return webhookEvents
                .findByStripeEventId(eventId)
                .isPresent();
    }

    @Transactional
    public void markProcessed(
            String eventId,
            String eventType
    ) {

        if (webhookEvents
                .findByStripeEventId(eventId)
                .isPresent()) {
            return;
        }

        StripeWebhookEvent event =
                new StripeWebhookEvent(
                        eventId,
                        eventType,
                        Instant.now()
                );

        webhookEvents.save(event);
    }

    // =========================================================
    // CHECKOUT COMPLETED
    // =========================================================

    @Transactional
    public void handleCheckoutCompleted(
            Session session
    ) {

        if (session.getMetadata() == null) {
            throw new IllegalArgumentException(
                    "Stripe checkout session is missing metadata"
            );
        }

        String userEmail =
                session.getMetadata().get("user_email");

        String planIdValue =
                session.getMetadata().get("plan_id");

        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe checkout session is missing user_email metadata"
            );
        }

        if (planIdValue == null || planIdValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe checkout session is missing plan_id metadata"
            );
        }

        Long planId;

        try {
            planId = Long.valueOf(planIdValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid plan_id in Stripe checkout session"
            );
        }

        User user =
                users.findByEmailIgnoreCase(userEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found: " + userEmail
                                )
                        );

        SubscriptionPlan plan =
                plans.findById(planId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subscription plan not found: "
                                                + planId
                                )
                        );

        String stripeSubscriptionId =
                session.getSubscription();

        String stripeCustomerId =
                session.getCustomer();

        // -----------------------------------------------------
        // Prevent duplicate subscription creation
        // -----------------------------------------------------

        if (stripeSubscriptionId != null &&
                !stripeSubscriptionId.isBlank()) {

            if (subscriptions
                    .findByStripeSubscriptionId(
                            stripeSubscriptionId
                    )
                    .isPresent()) {

                return;
            }
        }

        // -----------------------------------------------------
        // Check existing active subscription
        // -----------------------------------------------------

        Subscription existing =
                subscriptions
                        .findFirstByUserIdAndStatus(
                                user.getId(),
                                SubscriptionStatus.ACTIVE
                        )
                        .orElse(null);

        if (existing != null) {

            if (stripeSubscriptionId != null &&
                    stripeSubscriptionId.equals(
                            existing.getStripeSubscriptionId()
                    )) {

                return;
            }

            throw new IllegalStateException(
                    "User already has an active subscription"
            );
        }

        Instant now = Instant.now();

        // -----------------------------------------------------
        // Get initial renewal date directly from Stripe
        // -----------------------------------------------------

        Instant renewalDate =
                getSubscriptionRenewalDate(
                        stripeSubscriptionId
                );

        // -----------------------------------------------------
        // Create local subscription
        // -----------------------------------------------------

        Subscription subscription =
                new Subscription();

        subscription.setUser(user);
        subscription.setPlan(plan);

        subscription.setStatus(
                SubscriptionStatus.ACTIVE
        );

        subscription.setStartDate(now);
        subscription.setRenewalDate(renewalDate);
        subscription.setCancellationDate(null);

        subscription.setStripeSubscriptionId(
                stripeSubscriptionId
        );

        subscription.setStripeCustomerId(
                stripeCustomerId
        );

        subscription.setCreatedAt(now);
        subscription.setUpdatedAt(now);

        subscriptions.save(subscription);
    }

    // =========================================================
    // SUBSCRIPTION UPDATED
    // =========================================================

    @Transactional
    public void handleSubscriptionUpdated(
            com.stripe.model.Subscription stripeSubscription
    ) {

        String stripeSubscriptionId =
                stripeSubscription.getId();

        if (stripeSubscriptionId == null ||
                stripeSubscriptionId.isBlank()) {
            return;
        }

        Subscription subscription =
                subscriptions
                        .findByStripeSubscriptionId(
                                stripeSubscriptionId
                        )
                        .orElse(null);

        if (subscription == null) {
            return;
        }

        // -----------------------------------------------------
        // Stripe customer ID
        // -----------------------------------------------------

        if (stripeSubscription.getCustomer() != null) {

            subscription.setStripeCustomerId(
                    stripeSubscription.getCustomer()
            );
        }

        // -----------------------------------------------------
        // Subscription status
        // -----------------------------------------------------

        subscription.setStatus(
                mapStripeStatus(
                        stripeSubscription.getStatus()
                )
        );

        // -----------------------------------------------------
        // Renewal date
        // -----------------------------------------------------

        subscription.setRenewalDate(
                getCurrentPeriodEnd(
                        stripeSubscription
                )
        );

        // -----------------------------------------------------
        // Cancellation date
        // -----------------------------------------------------

        if (stripeSubscription.getCanceledAt() != null) {

            subscription.setCancellationDate(
                    Instant.ofEpochSecond(
                            stripeSubscription.getCanceledAt()
                    )
            );
        }

        subscription.setUpdatedAt(
                Instant.now()
        );

        subscriptions.save(subscription);
    }

    // =========================================================
    // SUBSCRIPTION DELETED / CANCELLED
    // =========================================================

    @Transactional
    public void handleSubscriptionDeleted(
            com.stripe.model.Subscription stripeSubscription
    ) {

        String stripeSubscriptionId =
                stripeSubscription.getId();

        if (stripeSubscriptionId == null ||
                stripeSubscriptionId.isBlank()) {
            return;
        }

        Subscription subscription =
                subscriptions
                        .findByStripeSubscriptionId(
                                stripeSubscriptionId
                        )
                        .orElse(null);

        if (subscription == null) {
            return;
        }

        subscription.setStatus(
                SubscriptionStatus.CANCELLED
        );

        if (stripeSubscription.getCanceledAt() != null) {

            subscription.setCancellationDate(
                    Instant.ofEpochSecond(
                            stripeSubscription.getCanceledAt()
                    )
            );

        } else {

            subscription.setCancellationDate(
                    Instant.now()
            );
        }

        subscription.setUpdatedAt(
                Instant.now()
        );

        subscriptions.save(subscription);
    }

    // =========================================================
    // PAYMENT FAILED
    // =========================================================

    @Transactional
    public void handleInvoicePaymentFailed(
            Invoice invoice
    ) {

        String stripeSubscriptionId =
                extractSubscriptionIdFromInvoice(invoice);

        if (stripeSubscriptionId == null ||
                stripeSubscriptionId.isBlank()) {

            return;
        }

        Subscription subscription =
                subscriptions
                        .findByStripeSubscriptionId(
                                stripeSubscriptionId
                        )
                        .orElse(null);

        if (subscription == null) {
            return;
        }

        subscription.setStatus(
                SubscriptionStatus.PAST_DUE
        );

        subscription.setUpdatedAt(
                Instant.now()
        );

        subscriptions.save(subscription);
    }

    // =========================================================
    // GET INITIAL SUBSCRIPTION RENEWAL DATE
    // =========================================================

    private Instant getSubscriptionRenewalDate(
            String stripeSubscriptionId
    ) {

        if (stripeSubscriptionId == null ||
                stripeSubscriptionId.isBlank()) {

            return null;
        }

        try {

            RequestOptions requestOptions =
                    RequestOptions.builder()
                            .setApiKey(stripeSecretKey)
                            .build();

            com.stripe.model.Subscription stripeSubscription =
                    com.stripe.model.Subscription.retrieve(
                            stripeSubscriptionId,
                            requestOptions
                    );

            System.out.println(
                    "Initial Stripe subscription found = "
                            + stripeSubscription.getId()
            );

            System.out.println(
                    "Initial Stripe subscription status = "
                            + stripeSubscription.getStatus()
            );

            Instant renewalDate =
                    getCurrentPeriodEnd(
                            stripeSubscription
                    );

            System.out.println(
                    "Initial Stripe renewal date = "
                            + renewalDate
            );

            return renewalDate;

        } catch (StripeException e) {

            throw new IllegalStateException(
                    "Unable to retrieve Stripe subscription "
                            + stripeSubscriptionId
                            + ": "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // EXTRACT SUBSCRIPTION ID FROM INVOICE
    // =========================================================

    private String extractSubscriptionIdFromInvoice(
            Invoice invoice
    ) {

        if (invoice == null) {
            return null;
        }

        if (invoice.getLines() == null ||
                invoice.getLines().getData() == null ||
                invoice.getLines().getData().isEmpty()) {

            return null;
        }

        for (InvoiceLineItem lineItem :
                invoice.getLines().getData()) {

            if (lineItem == null) {
                continue;
            }

            // Subscription item details

            if (lineItem.getParent() != null &&
                    lineItem.getParent()
                            .getSubscriptionItemDetails() != null) {

                String subscriptionId =
                        lineItem
                                .getParent()
                                .getSubscriptionItemDetails()
                                .getSubscription();

                if (subscriptionId != null &&
                        !subscriptionId.isBlank()) {

                    return subscriptionId;
                }
            }

            // Invoice item details

            if (lineItem.getParent() != null &&
                    lineItem.getParent()
                            .getInvoiceItemDetails() != null) {

                String subscriptionId =
                        lineItem
                                .getParent()
                                .getInvoiceItemDetails()
                                .getSubscription();

                if (subscriptionId != null &&
                        !subscriptionId.isBlank()) {

                    return subscriptionId;
                }
            }
        }

        return null;
    }

    // =========================================================
    // STRIPE STATUS → APPLICATION STATUS
    // =========================================================

    private SubscriptionStatus mapStripeStatus(
            String stripeStatus
    ) {

        if (stripeStatus == null) {
            return SubscriptionStatus.PAST_DUE;
        }

        return switch (
                stripeStatus.toLowerCase()
        ) {

            case "active", "trialing" ->
                    SubscriptionStatus.ACTIVE;

            case "past_due", "incomplete" ->
                    SubscriptionStatus.PAST_DUE;

            case "canceled",
                 "unpaid",
                 "incomplete_expired" ->
                    SubscriptionStatus.CANCELLED;

            default ->
                    SubscriptionStatus.PAST_DUE;
        };
    }

    // =========================================================
    // CURRENT BILLING PERIOD END
    // =========================================================

    private Instant getCurrentPeriodEnd(
            com.stripe.model.Subscription stripeSubscription
    ) {

        if (stripeSubscription == null ||
                stripeSubscription.getItems() == null ||
                stripeSubscription.getItems().getData() == null ||
                stripeSubscription.getItems().getData().isEmpty()) {

            return null;
        }

        SubscriptionItem item =
                stripeSubscription
                        .getItems()
                        .getData()
                        .get(0);

        if (item.getCurrentPeriodEnd() == null) {
            return null;
        }

        return Instant.ofEpochSecond(
                item.getCurrentPeriodEnd()
        );
    }

    // =========================================================
    // ATOMIC WEBHOOK CLAIM
    // =========================================================

    @Transactional
    public boolean claimEvent(
            String eventId,
            String eventType
    ) {

        return webhookEvents.claimEvent(
                eventId,
                eventType
        ) == 1;
    }
}