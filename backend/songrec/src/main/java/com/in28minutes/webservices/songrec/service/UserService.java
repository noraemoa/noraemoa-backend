package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.UpdateUserPasswordRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UpdateUsernameRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private User loadUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()-> new NotFoundException("해당 사용자를 찾을 수 없습니다."));
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Transactional
    public User updateUsername(UpdateUsernameRequestDto userDto, Long userId) {
        User user = loadUserOrThrow(userId);
        user.setUsername(userDto.getUsername());
        return user;
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Transactional
    public User updateUserPassword(UpdateUserPasswordRequestDto userDto, Long userId) {
        User user = loadUserOrThrow(userId);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return user;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<User> getUsers(){
        return userRepository.findAll();
    }

}
