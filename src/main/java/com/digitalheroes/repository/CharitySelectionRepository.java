package com.digitalheroes.repository;

import com.digitalheroes.entity.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface CharitySelectionRepository extends JpaRepository<CharitySelection, Long> {
	Optional<CharitySelection> findFirstByUserIdAndEffectiveToIsNull(Long userId);

	List<CharitySelection> findByUserIdOrderByEffectiveFromDesc(Long userId);
}