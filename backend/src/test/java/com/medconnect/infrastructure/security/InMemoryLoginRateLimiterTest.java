package com.medconnect.infrastructure.security;

import com.medconnect.domain.exception.DemasiadosIntentosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemoryLoginRateLimiterTest {

    @Test
    public void verificarPermitido_noLanza_siNoHuboIntentosPrevios() {
        InMemoryLoginRateLimiter limiter = new InMemoryLoginRateLimiter();

        assertDoesNotThrow(() -> limiter.verificarPermitido("ana@medconnect.com"));
    }

    @Test
    public void verificarPermitido_noLanza_conPocosIntentosFallidos() {
        InMemoryLoginRateLimiter limiter = new InMemoryLoginRateLimiter();

        for (int i = 0; i < 4; i++) {
            limiter.registrarFallo("ana@medconnect.com");
        }

        assertDoesNotThrow(() -> limiter.verificarPermitido("ana@medconnect.com"));
    }

    @Test
    public void verificarPermitido_lanza_trasCincoIntentosFallidos() {
        InMemoryLoginRateLimiter limiter = new InMemoryLoginRateLimiter();

        for (int i = 0; i < 5; i++) {
            limiter.registrarFallo("ana@medconnect.com");
        }

        assertThrows(DemasiadosIntentosException.class, () -> limiter.verificarPermitido("ana@medconnect.com"));
    }

    @Test
    public void registrarExito_reseteaElContador() {
        InMemoryLoginRateLimiter limiter = new InMemoryLoginRateLimiter();

        for (int i = 0; i < 5; i++) {
            limiter.registrarFallo("ana@medconnect.com");
        }
        limiter.registrarExito("ana@medconnect.com");

        assertDoesNotThrow(() -> limiter.verificarPermitido("ana@medconnect.com"));
    }

    @Test
    public void intentosFallidos_noAfectanAOtroEmail() {
        InMemoryLoginRateLimiter limiter = new InMemoryLoginRateLimiter();

        for (int i = 0; i < 5; i++) {
            limiter.registrarFallo("ana@medconnect.com");
        }

        assertDoesNotThrow(() -> limiter.verificarPermitido("otro@medconnect.com"));
    }

    @Test
    public void normalizaEmailPorMayusculasYEspacios() {
        InMemoryLoginRateLimiter limiter = new InMemoryLoginRateLimiter();

        for (int i = 0; i < 5; i++) {
            limiter.registrarFallo(" Ana@Medconnect.com ");
        }

        assertThrows(DemasiadosIntentosException.class, () -> limiter.verificarPermitido("ana@medconnect.com"));
    }
}
