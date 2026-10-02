package com.digitalheroes.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Table(name="charity_events") @Getter @Setter @NoArgsConstructor
public class CharityEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="charity_id",nullable=false) Charity charity;
 @Column(nullable=false) String title; @Column(columnDefinition="text") String description;
 @Column(name="event_date") LocalDate eventDate; @Column(name="created_at",nullable=false) Instant createdAt=Instant.now();
}
