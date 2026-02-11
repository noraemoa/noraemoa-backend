package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.Playlist;
import com.in28minutes.webservices.songrec.domain.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findAllByUserIdAndDeletedFalse(Long userId);
    boolean existsByUser_IdAndTemplate_Id(Long userId,Long templateId);
    Optional<Playlist> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
}
