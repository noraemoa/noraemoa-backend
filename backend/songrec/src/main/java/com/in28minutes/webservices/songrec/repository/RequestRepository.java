package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByUserId(Long userId);

    Optional<Request> findByIdAndUserId(Long id, Long userId);
}
