package com.digitalheroes.repository;

import com.digitalheroes.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
	List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc();
}