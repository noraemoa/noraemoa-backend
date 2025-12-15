package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.User;
import com.in28minutes.webservices.songrec.dto.request.UpdateUserPasswordRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UpdateUsernameRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UserCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()-> new NotFoundException("해당 사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public User createUser(UserCreateRequestDto userDto) {
        User user = User.builder()
                .name(userDto.getName())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User updateUsername(UpdateUsernameRequestDto userDto, Long userId) {
        User user = getUserById(userId);
        user.setName(userDto.getName());
        return user;
    }

    @Transactional
    public User updateUserPassword(UpdateUserPasswordRequestDto userDto, Long userId) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return user;
    }

    public boolean existUser(Long userId) {
        return userRepository.existsById(userId);
    }
}
