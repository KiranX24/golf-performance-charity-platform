package com.digitalheroes.repository;

import com.digitalheroes.entity.*;
import org.springframework.data.jpa.repository.*;
import java.time.*;
import java.util.*;

public interface ScoreRepository extends JpaRepository<Score, Long> {
	List<Score> findTop5ByUserIdOrderByScoreDateDesc(Long userId);

	boolean existsByUserIdAndScoreDate(Long userId, LocalDate date);

	Optional<Score> findByIdAndUserId(Long id, Long userId);

	long countByUserId(Long userId);
}