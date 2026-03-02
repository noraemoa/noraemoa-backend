package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.keyword.Keyword;
import com.in28minutes.webservices.songrec.domain.request.RequestKeyword;
import com.in28minutes.webservices.songrec.repository.projection.RequestKeywordRow;
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

    @Query("""
select 
  rk.request.id as requestId,
  k.id as keywordId,
  k.rawText as rawText
from RequestKeyword rk
join rk.keyword k
where rk.request.id in :requestIds
""")
    List<RequestKeywordRow> findKeywordRowsByRequestIds(@Param("requestIds") List<Long> requestIds);
    Optional<RequestKeyword> findByRequest_IdAndKeyword_Id(Long requestId, Long keywordId);
    void deleteByRequest_idAndKeyword_Id(Long requestId, Long keywordId);
}
