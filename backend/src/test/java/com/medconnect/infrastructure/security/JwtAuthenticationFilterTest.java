package com.medconnect.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JwtAuthenticationFilterTest {

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilter_noAutentica_siNoHayCookie() throws Exception {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    public void doFilter_autentica_siLaCookieJwtEsValida() throws Exception {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("ana@medconnect.com");
        when(claims.get("role", String.class)).thenReturn("MEDICO");
        when(jwtTokenService.validarYParsear("token-valido")).thenReturn(claims);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwt", "token-valido"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals("ana@medconnect.com", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals("ROLE_MEDICO", SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .iterator().next().getAuthority());
        verify(chain).doFilter(request, response);
    }

    @Test
    public void doFilter_ignoraOtrasCookies_siNoHayUnaLlamadaJwt() throws Exception {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("otra-cookie", "algo"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    public void doFilter_limpiaElContexto_siLaCookieEsInvalida() throws Exception {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        when(jwtTokenService.validarYParsear("token-invalido")).thenThrow(new JwtException("firma invalida"));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwt", "token-invalido"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}
