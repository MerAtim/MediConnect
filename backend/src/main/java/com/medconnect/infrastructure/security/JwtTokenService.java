package com.medconnect.infrastructure.security;

import com.medconnect.application.usecase.TokenService;
import com.medconnect.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenService implements TokenService {

    private static final String SECRETO_INSEGURO_POR_DEFECTO =
            "medconnect-dev-secret-please-override-in-production-min-32-bytes";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long expirationMs,
                            @Value("${app.cookie-secure}") boolean cookieSecure) {
        // cookie-secure=true indica un despliegue real (HTTPS); en ese caso no se puede
        // arrancar con el secreto de desarrollo hardcodeado en application.properties, o
        // cualquiera con acceso al repo podria forjar tokens validos de cualquier rol.
        if (cookieSecure && SECRETO_INSEGURO_POR_DEFECTO.equals(secret)) {
            throw new IllegalStateException(
                    "JWT_SECRET no fue configurado: no se puede arrancar con el secreto de desarrollo "
                            + "por defecto cuando COOKIE_SECURE=true (despliegue real).");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generar(Usuario usuario) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expirationMs);
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("role", usuario.getRole().name())
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(key)
                .compact();
    }

    public Claims validarYParsear(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
