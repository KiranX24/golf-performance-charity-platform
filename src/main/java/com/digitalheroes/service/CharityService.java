package com.digitalheroes.service;

import com.digitalheroes.dto.*;
import com.digitalheroes.entity.*;
import com.digitalheroes.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CharityService {

    private final CharityRepository charities;
    private final CharitySelectionRepository selections;
    private final DonationRepository donations;
    private final UserRepository users;

    public CharityService(
            CharityRepository charities,
            CharitySelectionRepository selections,
            DonationRepository donations,
            UserRepository users) {

        this.charities = charities;
        this.selections = selections;
        this.donations = donations;
        this.users = users;
    }

    // =========================
    // LIST CHARITIES
    // =========================

    public List<CharityResponse> list(String q) {

        List<Charity> c =
                (q == null || q.isBlank())
                        ? charities.findByArchivedFalseOrderByFeaturedDescNameAsc()
                        : charities.findByArchivedFalseAndNameContainingIgnoreCaseOrderByFeaturedDescNameAsc(q);

        return c.stream()
                .map(this::dto)
                .toList();
    }

    // =========================
    // GET CHARITY
    // =========================

    public CharityResponse get(Long id) {

        return dto(
                charities.findById(id)
                        .orElseThrow()
        );
    }

    // =========================
    // SELECT CHARITY
    // =========================

    @Transactional
    public CharitySelectionResponse select(
            String email,
            CharitySelectionRequest r) {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        Charity c = charities.findById(r.charityId())
                .orElseThrow();

        if (c.isArchived()) {
            throw new IllegalArgumentException(
                    "Charity is not active"
            );
        }

        // Close the user's previous active selection
        selections.findFirstByUserIdAndEffectiveToIsNull(u.getId())
                .ifPresent(old -> {

                    old.setEffectiveTo(Instant.now());

                    /*
                     * Flush immediately so the old current
                     * selection is closed before inserting
                     * the new one.
                     */
                    selections.saveAndFlush(old);
                });

        // Create new selection
        CharitySelection s = new CharitySelection();

        s.setUser(u);
        s.setCharity(c);
        s.setContributionPct(r.contributionPct());
        s.setEffectiveFrom(Instant.now());

        selections.save(s);

        return dto(s);
    }

    // =========================
    // CURRENT SELECTION
    // =========================

    public CharitySelectionResponse current(String email) {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        return selections
                .findFirstByUserIdAndEffectiveToIsNull(u.getId())
                .map(this::dto)
                .orElse(null);
    }

    // =========================
    // DONATION
    // =========================

    @Transactional
    public Donation donate(
            String email,
            DonationRequest r) {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        Charity c = charities.findById(r.charityId())
                .orElseThrow();

        Donation d = new Donation();

        d.setUser(u);
        d.setCharity(c);
        d.setAmount(r.amount());
        d.setStatus(DonationStatus.SUCCEEDED);
        d.setProviderTransactionId(
                "DEMO_DON_" + UUID.randomUUID()
        );

        return donations.save(d);
    }

    // =========================
    // CHARITY -> DTO
    // =========================

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

    // =========================
    // SELECTION -> DTO
    // =========================

    private CharitySelectionResponse dto(
            CharitySelection s) {

        return new CharitySelectionResponse(
                s.getCharity().getId(),
                s.getCharity().getName(),
                s.getContributionPct(),
                s.getEffectiveFrom()
        );
    }
}