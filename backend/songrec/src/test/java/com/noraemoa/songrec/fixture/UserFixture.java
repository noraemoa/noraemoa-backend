package com.noraemoa.songrec.fixture;

import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.domain.user.UserRole;

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
