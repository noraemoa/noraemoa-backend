package com.in28minutes.webservices.songrec.domain;

import com.in28minutes.webservices.songrec.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens",
        indexes = @Index(name = "idx_refresh_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false,unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "expires_at",nullable = false)
    private LocalDateTime expiresAt;

    // 이 Refresh Token이 죽었나?
    @Builder.Default
    @Column(nullable = false)
    private boolean revoked =false;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
