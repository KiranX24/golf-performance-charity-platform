package com.digitalheroes.service;

import com.digitalheroes.dto.*;
import com.digitalheroes.entity.*;
import com.digitalheroes.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository users;
    private final SubscriptionRepository subs;
    private final ScoreRepository scores;
    private final CharityRepository charities;
    private final DrawRepository draws;
    private final WinnerRepository winners;
    private final SubscriptionPlanRepository plans;

    public AdminService(
            UserRepository users,
            SubscriptionRepository subs,
            ScoreRepository scores,
            CharityRepository charities,
            DrawRepository draws,
            WinnerRepository winners,
            SubscriptionPlanRepository plans) {

        this.users = users;
        this.subs = subs;
        this.scores = scores;
        this.charities = charities;
        this.draws = draws;
        this.winners = winners;
        this.plans = plans;
    }

    // =========================
    // DASHBOARD
    // =========================

    public AdminStatsResponse stats() {

        long pending = winners.findAll().stream()
                .filter(w -> w.getVerificationStatus()
                        == VerificationStatus.PENDING_VERIFICATION)
                .count();

        return new AdminStatsResponse(
                users.count(),
                subs.findAll().stream()
                        .filter(s -> s.getStatus()
                                == SubscriptionStatus.ACTIVE)
                        .count(),
                scores.count(),
                charities.count(),
                draws.count(),
                winners.count(),
                pending
        );
    }

    // =========================
    // USERS
    // =========================

    public List<AdminUserResponse> users() {

        return users.findAll().stream()
                .map(u -> new AdminUserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getFullName(),
                        u.getRole(),
                        u.isActive()
                ))
                .toList();
    }

    @Transactional
    public AdminUserResponse setUserActive(
            Long id,
            boolean active) {

        User u = users.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"));

        u.setActive(active);
        u.setUpdatedAt(Instant.now());

        return new AdminUserResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole(),
                u.isActive()
        );
    }

    // =========================
    // CHARITIES
    // =========================

    @Transactional
    public CharityResponse createCharity(
            AdminCharityRequest r) {

        if (charities.findBySlug(r.slug()).isPresent()) {
            throw new IllegalArgumentException(
                    "Slug already exists"
            );
        }

        Charity c = new Charity();

        c.setName(r.name());
        c.setSlug(r.slug());
        c.setDescription(r.description());
        c.setLogoUrl(r.logoUrl());
        c.setFeatured(r.featured());

        // New charities are always active/unarchived.
        c.setArchived(false);

        Instant now = Instant.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);

        return dto(charities.save(c));
    }

    @Transactional
    public CharityResponse updateCharity(
            Long id,
            AdminCharityRequest r) {

        Charity c = charities.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Charity not found"));

        /*
         * Slug must remain unique.
         *
         * If the slug is changed, check whether another
         * charity already owns that slug.
         */
        if (!c.getSlug().equals(r.slug())
                && charities.findBySlug(r.slug()).isPresent()) {

            throw new IllegalArgumentException(
                    "Slug already exists"
            );
        }

        c.setName(r.name());
        c.setSlug(r.slug());
        c.setDescription(r.description());
        c.setLogoUrl(r.logoUrl());
        c.setFeatured(r.featured());
        c.setUpdatedAt(Instant.now());

        return dto(charities.save(c));
    }

    @Transactional
    public CharityResponse setCharityArchived(
            Long id,
            boolean archived) {

        Charity c = charities.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Charity not found"));

        c.setArchived(archived);
        c.setUpdatedAt(Instant.now());

        return dto(charities.save(c));
    }

    // =========================
    // PLANS
    // =========================

    @Transactional
    public PlanResponse createPlan(
            AdminPlanRequest r) {

        SubscriptionPlan p = new SubscriptionPlan();

        p.setName(r.name());
        p.setPlanInterval(r.interval());
        p.setPrice(r.price());
        p.setCurrency(r.currency());
        p.setActive(r.active());
        p.setStripePriceId(r.stripePriceId());

        p.setCreatedAt(Instant.now());

        p = plans.save(p);

        return planDto(p);
    }

    @Transactional
    public PlanResponse updatePlan(
            Long id,
            AdminPlanRequest r) {

        SubscriptionPlan p = plans.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subscription plan not found"));

        p.setName(r.name());
        p.setPlanInterval(r.interval());
        p.setPrice(r.price());
        p.setCurrency(r.currency());
        p.setStripePriceId(r.stripePriceId());

        /*
         * Keep the existing active/inactive state here.
         *
         * Activation/deactivation has its own endpoint.
         */
        p = plans.save(p);

        return planDto(p);
    }

    @Transactional
    public PlanResponse setPlanActive(
            Long id,
            boolean active) {

        SubscriptionPlan p = plans.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subscription plan not found"));

        p.setActive(active);

        p = plans.save(p);

        return planDto(p);
    }
    
    
    public List<PlanResponse> allPlans() {
        return plans.findAll()
                .stream()
                .map(this::planDto)
                .toList();
    }

    public List<CharityResponse> allCharities() {
        return charities.findAll()
                .stream()
                .map(this::dto)
                .toList();
    }
    

    // =========================
    // DTO HELPERS
    // =========================

    private PlanResponse planDto(SubscriptionPlan p) {
        return new PlanResponse(
                p.getId(),
                p.getName(),
                p.getPlanInterval(),
                p.getPrice(),
                p.getCurrency(),
                p.isActive()
        );
    }

    private CharityResponse dto(Charity c) {
        return new CharityResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getDescription(),
                c.getLogoUrl(),
                c.isFeatured(),
                c.isArchived()
        );
    }
}