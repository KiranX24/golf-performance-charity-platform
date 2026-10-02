package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DonationRepository extends JpaRepository<Donation,Long>{ List<Donation> findByUserIdOrderByCreatedAtDesc(Long userId); }