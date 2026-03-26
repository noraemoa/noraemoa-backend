package com.in28minutes.webservices.songrec.fixture;

import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.domain.user.UserRole;

public class UserFixture {
  public static User userRef(Long id){
    return User.builder().id(id).build();
  }
  public static User user(){
    return User.builder()
        .email("test@example.com")
        .username("testUser")
        .password("encodedPassword")
        .role(UserRole.USER)
        .build();
  }

}
