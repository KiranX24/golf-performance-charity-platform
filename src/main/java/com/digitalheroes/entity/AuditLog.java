package com.digitalheroes.entity;
import jakarta.persistence.*; import lombok.*; import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes; import java.time.Instant; import java.util.Map;
@Entity @Table(name="audit_logs") @Getter @Setter @NoArgsConstructor
public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="admin_id",nullable=false) User admin;
 @Column(nullable=false) String action; @Column(name="entity_type",nullable=false) String entityType; @Column(name="entity_id",nullable=false) Long entityId;
 @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb") Map<String,Object> metadata;
 @Column(name="created_at",nullable=false) Instant createdAt=Instant.now();
}
