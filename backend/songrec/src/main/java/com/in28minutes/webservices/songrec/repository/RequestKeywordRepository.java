package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.Keyword;
import com.in28minutes.webservices.songrec.domain.RequestKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RequestKeywordRepository extends JpaRepository<RequestKeyword, Long> {
    @Query("""
select rk.keyword
from RequestKeyword rk
where rk.request.id = :requestId
""")
    List<Keyword> findAllKeywordsByRequestId(@Param("requestId") Long requestId);
    Optional<RequestKeyword> findByRequest_IdAndKeyword_Id(Long requestId, Long keywordId);
}
