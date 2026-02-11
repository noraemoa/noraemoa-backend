package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.PlaylistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistTemplateRepository extends JpaRepository<PlaylistTemplate, Long> {
    List<PlaylistTemplate> findByIdBetween(long from, long to);
    boolean existsByCode(String code);
}
