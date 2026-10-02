package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PrizePoolRepository extends JpaRepository<PrizePool,Long>{ List<PrizePool> findByDrawId(Long drawId); Optional<PrizePool> findByDrawIdAndTier(Long drawId,PrizeTier tier); }