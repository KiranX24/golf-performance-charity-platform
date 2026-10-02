package com.digitalheroes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "draws")
public class Draw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "draw_period", nullable = false, unique = true)
    LocalDate drawPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ScoreMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DrawStatus status = DrawStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "drawn_numbers", columnDefinition = "smallint[]")
    Short[] drawnNumbers;

    @Column(name = "simulated_at")
    Instant simulatedAt;

    @Column(name = "published_at")
    Instant publishedAt;

    @Column(name = "completed_at")
    Instant completedAt;

    @Column(name = "cancelled_at")
    Instant cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy;

    @Column(name = "created_at", nullable = false)
    Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();

    // No-args constructor
    public Draw() {
    }

    // Getters

    public Long getId() {
        return id;
    }

    public LocalDate getDrawPeriod() {
        return drawPeriod;
    }

    public ScoreMode getMode() {
        return mode;
    }

    public DrawStatus getStatus() {
        return status;
    }

    public Short[] getDrawnNumbers() {
        return drawnNumbers;
    }

    public Instant getSimulatedAt() {
        return simulatedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setDrawPeriod(LocalDate drawPeriod) {
        this.drawPeriod = drawPeriod;
    }

    public void setMode(ScoreMode mode) {
        this.mode = mode;
    }

    public void setStatus(DrawStatus status) {
        this.status = status;
    }

    public void setDrawnNumbers(Short[] drawnNumbers) {
        this.drawnNumbers = drawnNumbers;
    }

    public void setSimulatedAt(Instant simulatedAt) {
        this.simulatedAt = simulatedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}