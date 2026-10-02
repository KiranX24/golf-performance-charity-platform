package com.digitalheroes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(
    name = "draw_participants",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_draw_user",
        columnNames = {"draw_id", "user_id"}
    )
)
public class DrawParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draw_id", nullable = false)
    Draw draw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(
        name = "snapshot_numbers",
        columnDefinition = "smallint[]",
        nullable = false
    )
    Short[] snapshotNumbers;

    @Column(name = "match_count")
    Short matchCount;

    @Column(name = "created_at", nullable = false)
    Instant createdAt = Instant.now();

    // No-args constructor
    public DrawParticipant() {
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Draw getDraw() {
        return draw;
    }

    public User getUser() {
        return user;
    }

    public Short[] getSnapshotNumbers() {
        return snapshotNumbers;
    }

    public Short getMatchCount() {
        return matchCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setDraw(Draw draw) {
        this.draw = draw;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSnapshotNumbers(Short[] snapshotNumbers) {
        this.snapshotNumbers = snapshotNumbers;
    }

    public void setMatchCount(Short matchCount) {
        this.matchCount = matchCount;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}