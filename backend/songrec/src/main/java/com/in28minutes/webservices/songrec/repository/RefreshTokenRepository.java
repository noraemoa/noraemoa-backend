package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
    void deleteAllByUser_Id(Long userId);
}
