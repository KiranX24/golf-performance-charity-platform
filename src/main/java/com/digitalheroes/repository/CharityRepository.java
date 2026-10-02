package com.digitalheroes.repository;

import com.digitalheroes.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CharityRepository extends JpaRepository<Charity, Long> {
	List<Charity> findByArchivedFalseOrderByFeaturedDescNameAsc();

	List<Charity> findByArchivedFalseAndNameContainingIgnoreCaseOrderByFeaturedDescNameAsc(String q);

	Optional<Charity> findBySlug(String slug);
}