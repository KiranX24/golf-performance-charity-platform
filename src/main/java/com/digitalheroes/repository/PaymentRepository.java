package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment,Long>{ List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId); }