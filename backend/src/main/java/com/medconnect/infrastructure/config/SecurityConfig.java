package com.medconnect.infrastructure.config;

import com.medconnect.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String allowedOriginPatterns;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           @Value("${app.allowed-origin-patterns}") String allowedOriginPatterns) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(allowedOriginPatterns.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // El JWT viaja en una cookie httpOnly: el navegador necesita permiso
        // explicito para mandarla/recibirla en requests cross-origin (front
        // y back corren en puertos distintos). Por eso el origen no puede
        // ser "*" -- el spec de CORS lo prohibe combinado con credenciales.
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // La cookie del JWT tiene SameSite=Lax, lo que ya bloquea que se
                // mande en requests POST/PUT/PATCH/DELETE disparados desde otro
                // sitio (CSRF clasico via form o fetch) -- sumado a que el CORS
                // de arriba no acepta "*", un sitio ajeno ni siquiera puede leer
                // la respuesta de un fetch. Por eso no hace falta el mecanismo de
                // token CSRF de Spring encima.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Documentacion de la API: es informacion de las rutas/DTOs, no de
                        // datos de pacientes, y el valor de Swagger UI (probar un endpoint
                        // sin armar curl a mano) se pierde si primero hay que autenticarse
                        // para verla.
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/me/contrasena").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/contrasena").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/medicos/me").hasRole("MEDICO")
                        .requestMatchers(HttpMethod.GET, "/api/pacientes/me").hasRole("PACIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/pacientes/emails-vinculados").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/medicos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/medicos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/medicos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/medicos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/pacientes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/pacientes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/pacientes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/pacientes/**").hasAnyRole("ADMINISTRADOR", "MEDICO")
                        .requestMatchers(HttpMethod.POST, "/api/turnos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/turnos/**").hasAnyRole("ADMINISTRADOR", "MEDICO", "PACIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/historias-clinicas/exportar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/historias-clinicas").hasRole("MEDICO")
                        .requestMatchers(HttpMethod.GET, "/api/historias-clinicas").hasRole("MEDICO")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
