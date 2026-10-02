package com.digitalheroes.service;

import com.digitalheroes.dto.PlanResponse;
import com.digitalheroes.dto.SubscriptionResponse;
import com.digitalheroes.entity.PlanInterval;
import com.digitalheroes.entity.Subscription;
import com.digitalheroes.entity.SubscriptionPlan;
import com.digitalheroes.entity.SubscriptionStatus;
import com.digitalheroes.entity.User;
import com.digitalheroes.repository.SubscriptionPlanRepository;
import com.digitalheroes.repository.SubscriptionRepository;
import com.digitalheroes.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import com.stripe.param.SubscriptionCancelParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SubscriptionService {

	private final SubscriptionRepository subscriptions;
	private final SubscriptionPlanRepository plans;
	private final UserRepository users;
	private final String stripeSecretKey;

	public SubscriptionService(SubscriptionRepository subscriptions, SubscriptionPlanRepository plans,
			UserRepository users, @Value("${app.stripe.secret-key}") String stripeSecretKey) {
		this.subscriptions = subscriptions;
		this.plans = plans;
		this.users = users;
		this.stripeSecretKey = stripeSecretKey;
        

	}

	public List<PlanResponse> plans() {

		return plans.findAll().stream().filter(SubscriptionPlan::isActive).map(this::planDto).toList();
	}

	@Transactional
	public SubscriptionResponse activate(String email, Long planId) {

		User user = users.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		SubscriptionPlan plan = plans.findById(planId)
				.orElseThrow(() -> new IllegalArgumentException("Subscription plan not found"));

		if (!plan.isActive()) {
			throw new IllegalArgumentException("Subscription plan is not active");
		}

		Subscription existing = subscriptions.findFirstByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
				.orElse(null);

		if (existing != null) {
			throw new IllegalStateException("User already has an active subscription");
		}

		Instant startDate = Instant.now();

		Instant renewalDate;

		if (plan.getPlanInterval() == PlanInterval.YEARLY) {
			renewalDate = startDate.plus(365, ChronoUnit.DAYS);
		} else {
			renewalDate = startDate.plus(30, ChronoUnit.DAYS);
		}

		Subscription subscription = new Subscription();

		subscription.setUser(user);
		subscription.setPlan(plan);
		subscription.setStatus(SubscriptionStatus.ACTIVE);
		subscription.setStartDate(startDate);
		subscription.setRenewalDate(renewalDate);
		subscription.setCancellationDate(null);
		subscription.setCreatedAt(startDate);
		subscription.setUpdatedAt(startDate);

		Subscription saved = subscriptions.save(subscription);

		return subscriptionDto(saved);
	}

	public List<SubscriptionResponse> mine(String email) {

		User user = users.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		return subscriptions.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::subscriptionDto)
				.toList();
	}

	public SubscriptionResponse cancel(String email) {

		User user = users.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Subscription subscription = subscriptions.findFirstByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
				.orElseThrow(() -> new IllegalStateException("No active subscription found"));

		String stripeSubscriptionId = subscription.getStripeSubscriptionId();

		if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {

			throw new IllegalStateException("This subscription is not linked to Stripe");
		}

		try {

			RequestOptions requestOptions = RequestOptions.builder().setApiKey(stripeSecretKey).build();

			com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription
					.retrieve(stripeSubscriptionId, requestOptions);

			
			// Already cancelled on Stripe
			if ("canceled".equalsIgnoreCase(stripeSubscription.getStatus())) {

			    subscription.setStatus(SubscriptionStatus.CANCELLED);

			    if (subscription.getCancellationDate() == null) {
			        subscription.setCancellationDate(Instant.now());
			    }

			    subscription.setUpdatedAt(Instant.now());

			    subscriptions.save(subscription);

			    return subscriptionDto(subscription);
			}
			
			
			SubscriptionCancelParams cancelParams = SubscriptionCancelParams.builder().build();

			stripeSubscription.cancel(cancelParams, requestOptions);

		} catch (StripeException e) {

			throw new IllegalStateException("Unable to cancel subscription on Stripe: " + e.getMessage(), e);
		}

		return subscriptionDto(subscription);
	}

	private PlanResponse planDto(SubscriptionPlan plan) {

		return new PlanResponse(plan.getId(), plan.getName(), plan.getPlanInterval(), plan.getPrice(),
				plan.getCurrency(), plan.isActive());
	}

	private SubscriptionResponse subscriptionDto(Subscription subscription) {

		SubscriptionPlan plan = subscription.getPlan();

		return new SubscriptionResponse(subscription.getId(), plan.getId(), plan.getName(), plan.getPlanInterval(),
				plan.getPrice(), subscription.getStatus(), subscription.getStartDate(), subscription.getRenewalDate(),
				subscription.getCancellationDate());
	}
}