package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByUserId(Long userId);
}
