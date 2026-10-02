package com.digitalheroes.service;

import com.digitalheroes.dto.*;
import com.digitalheroes.entity.*;
import com.digitalheroes.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class ScoreService {

    private final ScoreRepository scores;
    private final UserRepository users;
    private final SubscriptionRepository subs;

    public ScoreService(
            ScoreRepository scores,
            UserRepository users,
            SubscriptionRepository subs) {

        this.scores = scores;
        this.users = users;
        this.subs = subs;
    }

    @Transactional
    public ScoreResponse add(String email, ScoreRequest r) {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        if (subs.findFirstByUserIdAndStatus(
                u.getId(),
                SubscriptionStatus.ACTIVE
        ).isEmpty()) {

            throw new IllegalStateException(
                    "Active subscription required to manage scores"
            );
        }

        if (scores.existsByUserIdAndScoreDate(
                u.getId(),
                r.scoreDate()
        )) {

            throw new IllegalArgumentException(
                    "Only one score is allowed per date"
            );
        }

        Score s = new Score();

        s.setUser(u);
        s.setScoreValue(r.scoreValue());
        s.setScoreDate(r.scoreDate());

        scores.save(s);

        trim(u.getId());

        return dto(s);
    }

    public List<ScoreResponse> mine(String email) {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        return scores
                .findTop5ByUserIdOrderByScoreDateDesc(u.getId())
                .stream()
                .map(this::dto)
                .toList();
    }

    @Transactional
    public ScoreResponse update(
            String email,
            Long id,
            ScoreRequest r) {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        Score s = scores.findByIdAndUserId(
                id,
                u.getId()
        ).orElseThrow();

        if (scores.existsByUserIdAndScoreDate(
                u.getId(),
                r.scoreDate()
        ) && !s.getScoreDate().equals(r.scoreDate())) {

            throw new IllegalArgumentException(
                    "Only one score is allowed per date"
            );
        }

        s.setScoreValue(r.scoreValue());
        s.setScoreDate(r.scoreDate());
        s.setUpdatedAt(Instant.now());

        return dto(s);
    }

    @Transactional
    public void delete(String email, Long id) {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        scores.delete(
                scores.findByIdAndUserId(
                        id,
                        u.getId()
                ).orElseThrow()
        );
    }

    private void trim(Long uid) {

        List<Score> all = scores.findAll()
                .stream()
                .filter(s -> s.getUser()
                        .getId()
                        .equals(uid))
                .sorted(
                        Comparator.comparing(
                                Score::getScoreDate
                        ).reversed()
                )
                .toList();

        if (all.size() > 5) {
            scores.deleteAll(
                    all.subList(5, all.size())
            );
        }
    }

    private ScoreResponse dto(Score s) {

        return new ScoreResponse(
                s.getId(),
                s.getScoreValue(),
                s.getScoreDate()
        );
    }
}