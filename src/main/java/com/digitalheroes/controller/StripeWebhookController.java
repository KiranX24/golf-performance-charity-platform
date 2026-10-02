package com.digitalheroes.controller;

import com.digitalheroes.service.StripeWebhookService;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final StripeWebhookService webhookService;

    private final String webhookSecret;

    public StripeWebhookController(
            StripeWebhookService webhookService,

            @Value("${app.stripe.webhook-secret}")
            String webhookSecret
    ) {
        this.webhookService = webhookService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(

            @RequestBody String payload,

            @RequestHeader("Stripe-Signature")
            String signature

    ) {

        final Event event;

        // =====================================================
        // VERIFY STRIPE SIGNATURE
        // =====================================================

        try {

            event = Webhook.constructEvent(
                    payload,
                    signature,
                    webhookSecret
            );

        } catch (SignatureVerificationException e) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid Stripe signature");
        }

        String eventId = event.getId();

        String eventType = event.getType();

        try {

            // =================================================
            // ATOMIC IDEMPOTENCY CLAIM
            // =================================================
            /*
             * PostgreSQL ON CONFLICT DO NOTHING ensures that
             * two identical Stripe events arriving at the
             * same time cannot both process the event.
             */

            boolean claimed =
                    webhookService.claimEvent(
                            eventId,
                            eventType
                    );

            if (!claimed) {

                return ResponseEntity
                        .ok("Webhook already processed");
            }

            // =================================================
            // DESERIALIZE EVENT OBJECT
            // =================================================

            StripeObject stripeObject =
                    event.getDataObjectDeserializer()
                            .getObject()
                            .orElse(null);

            if (stripeObject == null) {

                throw new IllegalArgumentException(
                        "Unable to deserialize Stripe event: "
                                + eventType
                );
            }

            // =================================================
            // CHECKOUT COMPLETED
            // =================================================

            if ("checkout.session.completed"
                    .equals(eventType)) {

                if (stripeObject instanceof Session session) {

                    webhookService
                            .handleCheckoutCompleted(
                                    session
                            );
                }
            }

            // =================================================
            // SUBSCRIPTION UPDATED
            // =================================================

            else if ("customer.subscription.updated"
                    .equals(eventType)) {

                if (stripeObject instanceof
                        com.stripe.model.Subscription subscription) {

                    webhookService
                            .handleSubscriptionUpdated(
                                    subscription
                            );
                }
            }

            // =================================================
            // SUBSCRIPTION DELETED
            // =================================================

            else if ("customer.subscription.deleted"
                    .equals(eventType)) {

                if (stripeObject instanceof
                        com.stripe.model.Subscription subscription) {

                    webhookService
                            .handleSubscriptionDeleted(
                                    subscription
                            );
                }
            }

            // =================================================
            // PAYMENT FAILED
            // =================================================

            else if ("invoice.payment_failed"
                    .equals(eventType)) {

                if (stripeObject instanceof Invoice invoice) {

                    webhookService
                            .handleInvoicePaymentFailed(
                                    invoice
                            );
                }
            }

            // =================================================
            // SUCCESS
            // =================================================

            return ResponseEntity
                    .ok("Webhook received");

        } catch (Exception e) {

            e.printStackTrace();

            /*
             * IMPORTANT:
             *
             * If processing fails, transaction rollback will
             * remove the claimed event.
             *
             * Stripe will then retry the webhook.
             */

            return ResponseEntity
                    .internalServerError()
                    .body("Webhook processing failed");
        }
    }
}