package com.digitalheroes.repository;
import com.digitalheroes.entity.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{ }