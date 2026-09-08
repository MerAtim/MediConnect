package com.medconnect.infrastructure.security;

import com.medconnect.application.usecase.TokenRevocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// Revocacion de JWT en memoria por instancia: se resetea si el proceso
// reinicia y no se comparte entre instancias si el backend corre replicado
// -- mismo alcance y mismas limitaciones que InMemoryLoginRateLimiter,
// suficiente para el alcance actual del proyecto (una sola instancia).
@Component
public class InMemoryTokenRevocationService implements TokenRevocationService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryTokenRevocationService.class);

    private final Map<String, Long> cortePorEmail = new ConcurrentHashMap<>();
    private final long expirationMs;

    public InMemoryTokenRevocationService(@Value("${jwt.expiration-ms}") long expirationMs) {
        this.expirationMs = expirationMs;
    }

    @Override
    public void revocarTokensPrevios(String email) {
        cortePorEmail.put(normalizar(email), System.currentTimeMillis());
    }

    @Override
    public boolean fueRevocado(String email, Date emitidoEn) {
        Long corte = cortePorEmail.get(normalizar(email));
        return corte != null && emitidoEn != null && emitidoEn.getTime() <= corte;
    }

    // Una vez que pasa expirationMs desde el corte, cualquier token que
    // pudiera haber sido revocado por el ya expiro solo via su propio claim
    // `exp` -- la entrada queda redundante y se puede liberar.
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void limpiarCortesRedundantes() {
        long ahora = System.currentTimeMillis();
        int antes = cortePorEmail.size();
        cortePorEmail.values().removeIf(corte -> ahora - corte > expirationMs);
        int eliminadas = antes - cortePorEmail.size();
        if (eliminadas > 0) {
            log.debug("Limpieza de revocacion de tokens: {} entradas redundantes eliminadas", eliminadas);
        }
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
