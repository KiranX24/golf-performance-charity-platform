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

import java.time.Instant;

@Entity
@Table(name = "winner_proofs")
public class WinnerProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id", nullable = false)
    Winner winner;

    @Column(name = "storage_path", nullable = false)
    String storagePath;

    @Column(name = "file_type", nullable = false)
    String fileType;

    @Column(name = "file_size_bytes", nullable = false)
    long fileSizeBytes;

    @Column(name = "uploaded_at", nullable = false)
    Instant uploadedAt = Instant.now();

    // No-args constructor
    public WinnerProof() {
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Winner getWinner() {
        return winner;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setWinner(Winner winner) {
        this.winner = winner;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}