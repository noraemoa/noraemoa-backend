package com.in28minutes.webservices.songrec.config.security;

public record JwtPrincipal(Long userId, String role) {
}
