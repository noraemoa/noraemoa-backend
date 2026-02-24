package com.in28minutes.webservices.songrec.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    public JwtAuthFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
           @NotNull HttpServletRequest request,
           @NotNull HttpServletResponse response,
           @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if(token.isEmpty()){
            filterChain.doFilter(request, response);
            return;
        }

        try{
            Claims claims = jwtProvider.parseToken(token);

            Long userId=Long.parseLong(claims.getSubject());
            String role = String.valueOf(claims.get("role"));

//            1) 이 사람이 누구인지 확인 (principal)
//            2) 이 사람이 무엇을 할 수 있는지 설정 (authorities)
//            3) 이 요청을 “인증된 요청”으로 등록 (Authentication → SecurityContext)
            JwtPrincipal principal = new JwtPrincipal(userId,role);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_"+ role));

            var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }catch (JwtException | IllegalArgumentException e){
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
}
