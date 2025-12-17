package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByNormalizedText(String normalizedText);
}
