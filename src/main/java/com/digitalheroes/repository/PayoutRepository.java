package com.digitalheroes.repository;

import com.digitalheroes.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findAllByOrderByCreatedAtDesc();

    Optional<Payout> findByWinnerId(Long winnerId);
}