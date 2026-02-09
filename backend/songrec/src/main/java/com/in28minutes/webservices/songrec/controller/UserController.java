package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.User;
import com.in28minutes.webservices.songrec.dto.request.UpdateUserPasswordRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UpdateUsernameRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UserCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.UserResponseDto;
import com.in28minutes.webservices.songrec.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateRequestDto userDto) {
        User user = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(user));
    }

    @GetMapping
    public List<UserResponseDto> getUsers(){
        List<User> users =userService.getUsers();
        return users.stream().map(UserResponseDto::from).toList();
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable @NotNull @Positive Long userId) {
        User user = userService.getUserById(userId);
        return UserResponseDto.from(user);
    }

    @PatchMapping("/{userId}/name")
    public UserResponseDto updateUsername(
            @Valid @RequestBody UpdateUsernameRequestDto userDto,
            @PathVariable @NotNull @Positive Long userId){
        User user = userService.updateUsername(userDto, userId);
        return UserResponseDto.from(user);
    }
    @PatchMapping("/{userId}/password")
    public UserResponseDto updateUserPassword(
            @Valid @RequestBody UpdateUserPasswordRequestDto userDto,
            @PathVariable @NotNull @Positive Long userId){
        User user = userService.updateUserPassword(userDto, userId);
        return UserResponseDto.from(user);
    }
}
