package com.noraemoa.songrec.repository;

import com.noraemoa.songrec.domain.playlist.PlaylistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistTemplateRepository extends JpaRepository<PlaylistTemplate, Long> {
    List<PlaylistTemplate> findByIdBetween(long from, long to);
    boolean existsByCode(String code);
}
