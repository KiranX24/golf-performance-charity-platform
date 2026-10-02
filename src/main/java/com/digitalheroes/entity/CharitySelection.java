
package com.digitalheroes.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "charity_selections")
public class CharitySelection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "charity_id", nullable = false)
	Charity charity;

	@Column(name = "contribution_pct", nullable = false, precision = 5, scale = 2)
	BigDecimal contributionPct;

	@Column(name = "effective_from", nullable = false)
	Instant effectiveFrom = Instant.now();

	@Column(name = "effective_to")
	Instant effectiveTo;

	@Column(name = "created_at", nullable = false)
	Instant createdAt = Instant.now();

	// Getters

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Charity getCharity() {
		return charity;
	}

	public BigDecimal getContributionPct() {
		return contributionPct;
	}

	public Instant getEffectiveFrom() {
		return effectiveFrom;
	}

	public Instant getEffectiveTo() {
		return effectiveTo;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	// Setters

	public void setId(Long id) {
		this.id = id;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setCharity(Charity charity) {
		this.charity = charity;
	}

	public void setContributionPct(BigDecimal contributionPct) {
		this.contributionPct = contributionPct;
	}

	public void setEffectiveFrom(Instant effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public void setEffectiveTo(Instant effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	// No-args constructor

	public CharitySelection() {
	}
}
