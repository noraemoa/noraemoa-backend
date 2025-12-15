package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
