package com.digitalheroes.service;

import com.digitalheroes.dto.*;
import com.digitalheroes.entity.*;
import com.digitalheroes.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class DrawService {

    private final DrawRepository draws;
    private final DrawParticipantRepository participants;
    private final UserRepository users;
    private final ScoreRepository scores;
    private final SubscriptionRepository subscriptions;
    private final PrizePoolRepository prizePools;
    private final WinnerRepository winners;
    private final JackpotLedgerRepository jackpotRepo;
    private final AuditLogRepository audits;

    public DrawService(
            DrawRepository draws,
            DrawParticipantRepository participants,
            UserRepository users,
            ScoreRepository scores,
            SubscriptionRepository subscriptions,
            PrizePoolRepository prizePools,
            WinnerRepository winners,
            JackpotLedgerRepository jackpotRepo,
            AuditLogRepository audits) {

        this.draws = draws;
        this.participants = participants;
        this.users = users;
        this.scores = scores;
        this.subscriptions = subscriptions;
        this.prizePools = prizePools;
        this.winners = winners;
        this.jackpotRepo = jackpotRepo;
        this.audits = audits;
    }

    @Transactional
    public DrawResponse simulate(
            String adminEmail,
            DrawSimulationRequest req) {

        User admin = users.findByEmailIgnoreCase(adminEmail)
                .orElseThrow();

        Draw d = draws.findByDrawPeriod(req.drawPeriod())
                .orElseGet(() -> {

                    Draw x = new Draw();

                    x.setDrawPeriod(req.drawPeriod());
                    x.setCreatedBy(admin);

                    return x;
                });

        if (d.getStatus() == DrawStatus.PUBLISHED
                || d.getStatus() == DrawStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Published draw cannot be simulated again"
            );
        }

        d.setMode(req.mode());
        d.setStatus(DrawStatus.SIMULATED);
        d.setSimulatedAt(Instant.now());
        d.setUpdatedAt(Instant.now());

        draws.save(d);

        participants.findByDrawId(d.getId())
                .forEach(participants::delete);

        List<User> eligible = users.findAll()
                .stream()
                .filter(u ->
                        u.isActive()
                                && subscriptions
                                .findFirstByUserIdAndStatus(
                                        u.getId(),
                                        SubscriptionStatus.ACTIVE
                                )
                                .isPresent()
                                && scores
                                .findTop5ByUserIdOrderByScoreDateDesc(
                                        u.getId()
                                )
                                .size() == 5
                )
                .toList();

        List<Short[]> nums = new ArrayList<>();

        for (User u : eligible) {

            List<Score> ss =
                    scores.findTop5ByUserIdOrderByScoreDateDesc(
                            u.getId()
                    );

            /*
             * Score.getScoreValue() returns primitive short.
             * Explicitly convert short -> Short.
             */
            Short[] a = ss.stream()
                    .map(s -> Short.valueOf(s.getScoreValue()))
                    .toArray(Short[]::new);

            DrawParticipant p = new DrawParticipant();

            p.setDraw(d);
            p.setUser(u);
            p.setSnapshotNumbers(a);

            participants.save(p);

            nums.add(a);
        }

        Short[] drawn =
                req.mode() == ScoreMode.RANDOM
                        ? randomNumbers(5)
                        : weightedNumbers(nums);

        d.setDrawnNumbers(drawn);

        draws.save(d);

        return dto(d, eligible.size());
    }

    @Transactional
    public DrawResponse publish(
            String adminEmail,
            Long id) {

        User admin = users.findByEmailIgnoreCase(adminEmail)
                .orElseThrow();

        Draw d = draws.findById(id)
                .orElseThrow();

        if (d.getStatus() != DrawStatus.SIMULATED) {

            throw new IllegalStateException(
                    "Only simulated draws can be published"
            );
        }

        d.setStatus(DrawStatus.PUBLISHED);
        d.setPublishedAt(Instant.now());
        d.setUpdatedAt(Instant.now());

        draws.save(d);

        List<DrawParticipant> ps =
                participants.findByDrawId(id);

        for (DrawParticipant p : ps) {

            short m = matches(
                    p.getSnapshotNumbers(),
                    d.getDrawnNumbers()
            );

            p.setMatchCount(m);

            participants.save(p);
        }

        allocatePrizePools(d);

        createWinners(d);

        return dto(d, ps.size());
    }

    private void allocatePrizePools(Draw d) {

        BigDecimal base = BigDecimal.ZERO;

        for (DrawParticipant p :
                participants.findByDrawId(d.getId())) {

            var sub = subscriptions
                    .findFirstByUserIdAndStatus(
                            p.getUser().getId(),
                            SubscriptionStatus.ACTIVE
                    );

            if (sub.isPresent()) {

                base = base.add(
                        sub.get().getPlan().getPrice()
                );
            }
        }

        base = base
                .multiply(new BigDecimal("0.20"))
                .setScale(2, RoundingMode.HALF_UP);

        JackpotLedger ledger = jackpotRepo.getLedger();

        BigDecimal[] pct = {
                new BigDecimal(".25"),
                new BigDecimal(".35"),
                new BigDecimal(".40")
        };

        PrizeTier[] tiers = {
                PrizeTier.THREE,
                PrizeTier.FOUR,
                PrizeTier.FIVE
        };

        for (int i = 0; i < 3; i++) {

            PrizePool pp = new PrizePool();

            pp.setDraw(d);
            pp.setTier(tiers[i]);

            pp.setAllocatedAmount(
                    base.multiply(pct[i])
                            .setScale(2, RoundingMode.HALF_UP)
            );

            pp.setRolloverIn(
                    i == 2
                            ? ledger.getBalance()
                            : BigDecimal.ZERO
            );

            pp.setTotalPool(
                    pp.getAllocatedAmount()
                            .add(pp.getRolloverIn())
            );

            prizePools.save(pp);
        }
    }

    private void createWinners(Draw d) {

        List<DrawParticipant> ps =
                participants.findByDrawId(d.getId());

        for (PrizeTier tier : PrizeTier.values()) {

            short required =
                    tier == PrizeTier.THREE
                            ? (short) 3
                            : (tier == PrizeTier.FOUR
                                    ? (short) 4
                                    : (short) 5);

            List<DrawParticipant> ws = ps.stream()
                    .filter(p ->
                            p.getMatchCount() != null
                                    && p.getMatchCount() == required
                    )
                    .toList();

            PrizePool pp =
                    prizePools
                            .findByDrawIdAndTier(
                                    d.getId(),
                                    tier
                            )
                            .orElseThrow();

            pp.setWinnerCount(ws.size());

            if (ws.isEmpty()) {

                if (tier == PrizeTier.FIVE) {

                    JackpotLedger l =
                            jackpotRepo.getLedger();

                    l.setBalance(pp.getTotalPool());
                    l.setLastDraw(d);
                    l.setUpdatedAt(Instant.now());

                    jackpotRepo.save(l);

                    pp.setRolloverOut(
                            pp.getTotalPool()
                    );
                }

                prizePools.save(pp);

                continue;
            }

            BigDecimal per =
                    pp.getTotalPool()
                            .divide(
                                    BigDecimal.valueOf(ws.size()),
                                    2,
                                    RoundingMode.DOWN
                            );

            BigDecimal residual =
                    pp.getTotalPool()
                            .subtract(
                                    per.multiply(
                                            BigDecimal.valueOf(ws.size())
                                    )
                            );

            pp.setAmountPerWinner(per);
            pp.setRoundingResidual(residual);
            pp.setRolloverOut(BigDecimal.ZERO);

            prizePools.save(pp);

            for (DrawParticipant p : ws) {

                Winner w = new Winner();

                w.setDraw(d);
                w.setUser(p.getUser());
                w.setTier(tier);
                w.setAmount(per);

                winners.save(w);
            }

            if (tier == PrizeTier.FIVE) {

                JackpotLedger l =
                        jackpotRepo.getLedger();

                l.setBalance(BigDecimal.ZERO);
                l.setLastDraw(d);
                l.setUpdatedAt(Instant.now());

                jackpotRepo.save(l);
            }
        }
    }

    private short matches(
            Short[] a,
            Short[] b) {

        Set<Short> s =
                new HashSet<>(Arrays.asList(a));

        short n = 0;

        for (Short x :
                new HashSet<>(Arrays.asList(b))) {

            if (s.contains(x)) {
                n++;
            }
        }

        return n;
    }

    private Short[] randomNumbers(int count) {

        List<Short> n = new ArrayList<>();

        for (short i = 1; i <= 45; i++) {
            n.add(i);
        }

        Collections.shuffle(n);

        return n.subList(0, count)
                .toArray(new Short[0]);
    }

    private Short[] weightedNumbers(
            List<Short[]> nums) {

        Map<Short, Integer> f =
                new HashMap<>();

        for (Short[] a : nums) {

            for (Short x : a) {

                f.merge(
                        x,
                        1,
                        Integer::sum
                );
            }
        }

        List<Short> pool = new ArrayList<>();

        for (short i = 1; i <= 45; i++) {
            pool.add(i);
        }

        List<Short> out = new ArrayList<>();

        Random r = new Random();

        while (out.size() < 5 && !pool.isEmpty()) {

            int total = pool.stream()
                    .mapToInt(
                            x -> f.getOrDefault(x, 0) + 1
                    )
                    .sum();

            int pick = r.nextInt(total);

            for (Short x : pool) {

                pick -=
                        f.getOrDefault(x, 0) + 1;

                if (pick < 0) {

                    out.add(x);
                    pool.remove(x);

                    break;
                }
            }
        }

        return out.toArray(new Short[0]);
    }

    public List<DrawResponse> all() {

        return draws
                .findAllByOrderByDrawPeriodDesc()
                .stream()
                .map(d ->
                        dto(
                                d,
                                participants
                                        .findByDrawId(d.getId())
                                        .size()
                        )
                )
                .toList();
    }

    public DrawResponse get(Long id) {

        Draw d = draws.findById(id)
                .orElseThrow();

        return dto(
                d,
                participants.findByDrawId(id).size()
        );
    }

    private DrawResponse dto(
            Draw d,
            int count) {

        return new DrawResponse(
                d.getId(),
                d.getDrawPeriod(),
                d.getMode(),
                d.getStatus(),
                d.getDrawnNumbers(),
                d.getSimulatedAt(),
                d.getPublishedAt(),
                count
        );
    }
}