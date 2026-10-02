package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CharityEventRepository extends JpaRepository<CharityEvent,Long>{ List<CharityEvent> findByCharityIdOrderByEventDateAsc(Long charityId); }