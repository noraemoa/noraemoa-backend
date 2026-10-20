package com.noraemoa.songrec.controller;

import com.noraemoa.songrec.config.security.JwtPrincipal;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.dto.request.UpdateUserPasswordRequestDto;
import com.noraemoa.songrec.dto.request.UpdateUsernameRequestDto;
import com.noraemoa.songrec.dto.response.user.UserResponseDto;
import com.noraemoa.songrec.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponseDto> getUsers(){
        List<User> users =userService.getUsers();
        return users.stream().map(UserResponseDto::from).toList();
    }

    // admin or 본인
    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable @NotNull @Positive Long userId) {
        User user = userService.getUserById(userId);
        return UserResponseDto.from(user);
    }

    @GetMapping("/me")
    public UserResponseDto getUser(@AuthenticationPrincipal JwtPrincipal principal) {
        User user = userService.getUserById(principal.userId());
        return UserResponseDto.from(user);
    }

    @PatchMapping("/me/username")
    public UserResponseDto updateUsername(
            @Valid @RequestBody UpdateUsernameRequestDto userDto,
        @AuthenticationPrincipal JwtPrincipal principal){
        User user = userService.updateUsername(userDto, principal.userId());
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
