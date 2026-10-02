package com.digitalheroes.controller;

import com.digitalheroes.dto.PlanResponse;
import com.digitalheroes.dto.SubscriptionResponse;
import com.digitalheroes.service.StripeService;
import com.digitalheroes.service.SubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SubscriptionController {

    private final SubscriptionService service;
    private final StripeService stripeService;

    public SubscriptionController(
            SubscriptionService service,
            StripeService stripeService
    ) {
        this.service = service;
        this.stripeService = stripeService;
    }

    @GetMapping("/plans")
    public List<PlanResponse> plans() {
        return service.plans();
    }

    @PostMapping("/subscriptions/checkout/{planId}")
    public Map<String, String> checkout(
            Authentication a,
            @PathVariable Long planId
    ) throws Exception {

        String checkoutUrl = stripeService.createCheckoutSession(
                planId,
                a.getName()
        );

        return Map.of("checkoutUrl", checkoutUrl);
    }

    @PostMapping("/subscriptions/activate/{planId}")
    public SubscriptionResponse activate(
            Authentication a,
            @PathVariable Long planId
    ) {
        return service.activate(a.getName(), planId);
    }

    @GetMapping("/subscriptions/me")
    public List<SubscriptionResponse> mine(Authentication a) {
        return service.mine(a.getName());
    }

    @PostMapping("/subscriptions/cancel")
    public SubscriptionResponse cancel(Authentication a) {
        return service.cancel(a.getName());
    }
}