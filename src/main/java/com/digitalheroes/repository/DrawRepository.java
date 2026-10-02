package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.*; import java.util.*;
public interface DrawRepository extends JpaRepository<Draw,Long>{ Optional<Draw> findByDrawPeriod(LocalDate period); List<Draw> findAllByOrderByDrawPeriodDesc(); }