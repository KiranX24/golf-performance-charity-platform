package com.digitalheroes.service;

import com.digitalheroes.dto.VerificationRequest;
import com.digitalheroes.dto.WinnerResponse;
import com.digitalheroes.entity.Payout;
import com.digitalheroes.entity.PayoutStatus;
import com.digitalheroes.entity.User;
import com.digitalheroes.entity.VerificationStatus;
import com.digitalheroes.entity.Winner;
import com.digitalheroes.entity.WinnerProof;
import com.digitalheroes.repository.PayoutRepository;
import com.digitalheroes.repository.UserRepository;
import com.digitalheroes.repository.WinnerProofRepository;
import com.digitalheroes.repository.WinnerRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class WinnerService {

    private final WinnerRepository winners;
    private final UserRepository users;
    private final WinnerProofRepository proofs;
    private final PayoutRepository payouts;

    private final Path uploadRoot = Paths.get(
            System.getProperty("java.io.tmpdir"),
            "digital-heroes-proofs"
    );

    public WinnerService(
            WinnerRepository winners,
            UserRepository users,
            WinnerProofRepository proofs,
            PayoutRepository payouts) {

        this.winners = winners;
        this.users = users;
        this.proofs = proofs;
        this.payouts = payouts;
    }

    // =========================================================
    // USER WINNERS
    // =========================================================

    @Transactional
    public List<WinnerResponse> mine(String email) {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        return winners.findByUserIdOrderByCreatedAtDesc(u.getId())
                .stream()
                .map(this::dto)
                .toList();
    }

    // =========================================================
    // UPLOAD WINNER PROOF
    // =========================================================

    @Transactional
    public String uploadProof(
            String email,
            Long winnerId,
            MultipartFile file) throws IOException {

        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow();

        Winner w = winners.findById(winnerId)
                .orElseThrow();

        if (!w.getUser().getId().equals(u.getId())) {
            throw new IllegalArgumentException(
                    "Winner does not belong to current user"
            );
        }

        if (file.isEmpty() || file.getSize() > 5_000_000) {
            throw new IllegalArgumentException(
                    "File must be non-empty and <= 5 MB"
            );
        }

        String type = file.getContentType() == null
                ? ""
                : file.getContentType();

        if (!type.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "Only image proof files are allowed"
            );
        }

        Files.createDirectories(uploadRoot);

        String name =
                UUID.randomUUID()
                        + "_"
                        + Path.of(
                                Objects.requireNonNull(
                                        file.getOriginalFilename()
                                )
                        ).getFileName();

        Path target = uploadRoot.resolve(name);

        Files.copy(
                file.getInputStream(),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );

        WinnerProof p = new WinnerProof();

        p.setWinner(w);
        p.setStoragePath(target.toString());
        p.setFileType(type);
        p.setFileSizeBytes(file.getSize());

        proofs.save(p);

        return "Proof uploaded successfully";
    }

    // =========================================================
    // ADMIN VERIFY WINNER
    // =========================================================

    @Transactional
    public WinnerResponse verify(
            String adminEmail,
            Long winnerId,
            VerificationRequest r) {

        User admin = users.findByEmailIgnoreCase(adminEmail)
                .orElseThrow();

        Winner w = winners.findById(winnerId)
                .orElseThrow();

        boolean approved = Boolean.TRUE.equals(r.approved());

        // -----------------------------------------------------
        // APPROVAL REQUIRES PROOF
        // -----------------------------------------------------

        if (approved) {

            List<WinnerProof> proofList =
                    proofs.findByWinnerId(winnerId);

            if (proofList.isEmpty()) {

                throw new IllegalStateException(
                        "Winner proof is required before approval"
                );
            }
        }

        // -----------------------------------------------------
        // UPDATE WINNER STATUS
        // -----------------------------------------------------

        w.setVerificationStatus(
                approved
                        ? VerificationStatus.APPROVED
                        : VerificationStatus.REJECTED
        );

        w.setVerificationNotes(r.notes());
        w.setVerifiedBy(admin);
        w.setVerifiedAt(Instant.now());

        // -----------------------------------------------------
        // CREATE PAYOUT WHEN APPROVED
        // -----------------------------------------------------

        if (approved) {

            Payout p =
                    payouts.findByWinnerId(winnerId)
                            .orElse(null);

            if (p == null) {

                p = new Payout();

                p.setWinner(w);
                p.setAmount(w.getAmount());
                p.setStatus(PayoutStatus.PENDING);

                payouts.save(p);
            }
        }

        return dto(w);
    }

    // =========================================================
    // ADMIN MARK PAYOUT AS PAID
    // =========================================================

    @Transactional
    public PayoutResponseDto markPaid(
            String adminEmail,
            Long payoutId) {

        User admin = users.findByEmailIgnoreCase(adminEmail)
                .orElseThrow();

        Payout p = payouts.findById(payoutId)
                .orElseThrow();

        if (p.getStatus() == PayoutStatus.PAID) {
            throw new IllegalStateException(
                    "Payout is already marked as paid"
            );
        }

        p.setStatus(PayoutStatus.PAID);
        p.setPaidAt(Instant.now());
        p.setMarkedBy(admin);
        p.setUpdatedAt(Instant.now());

        payouts.save(p);

        return new PayoutResponseDto(
                p.getId(),
                p.getWinner().getId(),
                p.getAmount(),
                p.getStatus(),
                p.getPaidAt()
        );
    }

    // =========================================================
    // ADMIN ALL WINNERS
    // =========================================================

    @Transactional
    public List<WinnerResponse> all() {

        return winners.findAll()
                .stream()
                .map(this::dto)
                .toList();
    }

    // =========================================================
    // WINNER -> RESPONSE DTO
    // =========================================================

    private WinnerResponse dto(Winner w) {

        Payout payout =
                payouts.findByWinnerId(w.getId())
                        .orElse(null);

        return new WinnerResponse(
                w.getId(),
                w.getDraw().getId(),
                w.getUser().getId(),
                w.getUser().getFullName(),
                w.getTier(),
                w.getAmount(),
                w.getVerificationStatus(),

                payout != null
                        ? payout.getId()
                        : null,

                payout != null
                        ? payout.getStatus()
                        : null,

                payout != null
                        ? payout.getPaidAt()
                        : null
        );
    }

    // =========================================================
    // PAYOUT RESPONSE
    // =========================================================

    public record PayoutResponseDto(
            Long id,
            Long winnerId,
            BigDecimal amount,
            PayoutStatus status,
            Instant paidAt
    ) {
    }
}