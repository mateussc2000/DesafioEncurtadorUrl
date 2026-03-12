package com.encurtador_url.SuperApp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma URL encurtada
 */
@Entity
@Table(name = "shortened_urls", indexes = {
    @Index(name = "idx_custom_alias", columnList = "custom_alias", unique = true),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_short_url", columnList = "short_url", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenedUrl {

    @Id
    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(name = "short_url", unique = true, length = 512)
    private String shortUrl;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Integer clickCount = 0;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "custom_alias", length = 50)
    private String customAlias;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
