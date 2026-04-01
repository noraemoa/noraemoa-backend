package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.user.UserPreferenceTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceTagRepository extends JpaRepository<UserPreferenceTag, Long> {
  void deleteAllByUser_Id(Long user_id) ;
  List<UserPreferenceTag> findAllByUser_id(Long user_id);
}
