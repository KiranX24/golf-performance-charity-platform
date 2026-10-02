package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface WinnerProofRepository extends JpaRepository<WinnerProof,Long>{ List<WinnerProof> findByWinnerId(Long winnerId); }