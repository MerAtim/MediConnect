package com.medconnect.infrastructure.security;

import com.medconnect.application.usecase.TokenRevocationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "jwt";

    private final JwtTokenService jwtTokenService;
    private final TokenRevocationService tokenRevocationService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, TokenRevocationService tokenRevocationService) {
        this.jwtTokenService = jwtTokenService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extraerToken(request);

        if (token != null) {
            try {
                Claims claims = jwtTokenService.validarYParsear(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                // MEDIUM de la re-auditoria e2e (2026-09-08): la firma y el
                // vencimiento ya los valido validarYParsear() arriba (tira
                // JwtException si estan mal); esto cubre el caso que la firma
                // sola no puede cubrir -- un token todavia sin vencer pero
                // revocado explicitamente (logout, cambio de contrasena).
                if (tokenRevocationService.fueRevocado(email, claims.getIssuedAt())) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                var authentication = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
