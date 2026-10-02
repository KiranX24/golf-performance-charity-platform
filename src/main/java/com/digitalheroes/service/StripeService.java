package com.digitalheroes.service;

import com.digitalheroes.entity.SubscriptionPlan;
import com.digitalheroes.repository.SubscriptionPlanRepository;
import com.stripe.StripeClient;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    private final SubscriptionPlanRepository plans;
    private final StripeClient stripeClient;

    private final String successUrl;
    private final String cancelUrl;

    public StripeService(
            SubscriptionPlanRepository plans,
            @Value("${app.stripe.secret-key}") String secretKey,
            @Value("${app.stripe.success-url}") String successUrl,
            @Value("${app.stripe.cancel-url}") String cancelUrl) {

        this.plans = plans;
        this.stripeClient = new StripeClient(secretKey);
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    public String createCheckoutSession(
            Long planId,
            String userEmail) throws Exception {

        SubscriptionPlan plan = plans.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subscription plan not found"));

        if (!plan.isActive()) {
            throw new IllegalArgumentException(
                    "Subscription plan is not active");
        }

        if (plan.getStripePriceId() == null ||
                plan.getStripePriceId().isBlank()) {

            throw new IllegalArgumentException(
                    "Stripe Price ID is not configured for this plan");
        }

        SessionCreateParams params =
                SessionCreateParams.builder()

                        // Stripe creates a recurring subscription
                        .setMode(
                                SessionCreateParams.Mode.SUBSCRIPTION
                        )

                        // Where Stripe sends the user after checkout
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)

                        // Stripe customer email
                        .setCustomerEmail(userEmail)

                        // IMPORTANT:
                        // Webhook will use this to identify the user.
                        .setClientReferenceId(userEmail)

                        // IMPORTANT:
                        // Store our application data inside Stripe session.
                        .putMetadata(
                                "user_email",
                                userEmail
                        )
                        .putMetadata(
                                "plan_id",
                                String.valueOf(planId)
                        )

                        // Plan price
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(plan.getStripePriceId())
                                        .setQuantity(1L)
                                        .build()
                        )

                        .build();

        Session session =
                stripeClient.v1()
                        .checkout()
                        .sessions()
                        .create(params);

        return session.getUrl();
    }
}