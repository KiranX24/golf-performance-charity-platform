package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DrawParticipantRepository extends JpaRepository<DrawParticipant,Long>{ List<DrawParticipant> findByDrawId(Long drawId); }