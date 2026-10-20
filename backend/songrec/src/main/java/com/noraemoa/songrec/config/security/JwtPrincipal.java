package com.noraemoa.songrec.config.security;

public record JwtPrincipal(Long userId, String role) {
}
