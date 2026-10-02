package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface JackpotLedgerRepository extends JpaRepository<JackpotLedger,Long>{ default JackpotLedger getLedger(){ return findById(1L).orElseThrow(); } }