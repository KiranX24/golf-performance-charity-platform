package com.digitalheroes.service;

import com.digitalheroes.dto.*;
import com.digitalheroes.entity.User;

import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final AuthService auth;
    private final SubscriptionService subs;
    private final ScoreService scores;
    private final CharityService charities;
    private final WinnerService winners;

    public DashboardService(
            AuthService auth,
            SubscriptionService subs,
            ScoreService scores,
            CharityService charities,
            WinnerService winners) {

        this.auth = auth;
        this.subs = subs;
        this.scores = scores;
        this.charities = charities;
        this.winners = winners;
    }

    public DashboardResponse user(String email) {

        UserResponse u =
                auth.toDto(auth.current(email));

        var s = subs.mine(email);
        var sc = scores.mine(email);
        var ch = charities.current(email);
        var w = winners.mine(email);

        return new DashboardResponse(
                u,
                s.isEmpty() ? null : s.get(0),
                sc,
                ch,
                w
        );
    }
}