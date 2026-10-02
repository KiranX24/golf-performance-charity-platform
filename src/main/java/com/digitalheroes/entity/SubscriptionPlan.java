package com.digitalheroes.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	@Column(nullable = false)
	String name;
	@Enumerated(EnumType.STRING)
	@Column(name = "plan_interval", nullable = false)
	PlanInterval planInterval;
	@Column(nullable = false, precision = 10, scale = 2)
	BigDecimal price;
	@Column(nullable = false, length = 3)
	String currency = "INR";
	@Column(name = "is_active", nullable = false)
	boolean active = true;
	@Column(name = "stripe_price_id")
	String stripePriceId;
	@Column(name = "created_at", nullable = false)
	Instant createdAt = Instant.now();

	public SubscriptionPlan() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PlanInterval getPlanInterval() {
		return planInterval;
	}

	public void setPlanInterval(PlanInterval planInterval) {
		this.planInterval = planInterval;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getStripePriceId() {
		return stripePriceId;
	}

	public void setStripePriceId(String stripePriceId) {
		this.stripePriceId = stripePriceId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
