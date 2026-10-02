package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface WinnerRepository extends JpaRepository<Winner,Long>{ List<Winner> findByUserIdOrderByCreatedAtDesc(Long userId); List<Winner> findByDrawId(Long drawId); }