package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.request.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByUserIdAndDeletedFalse(Long userId);
    List<Request> findAllByDeletedFalse();
    Optional<Request> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
    Optional<Request> findByIdAndDeletedFalse(Long id);

    @Query("""
select r
from Request r
join fetch r.user username
where r.deleted=false
order by r.createdAt desc
""")
    Page<Request> findFeed(Pageable pageable);
}
