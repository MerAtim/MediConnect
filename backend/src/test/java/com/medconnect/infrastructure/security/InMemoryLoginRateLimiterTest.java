package com.medconnect.infrastructure.security;

import com.medconnect.domain.exception.DemasiadosIntentosException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // MEDIUM de la re-auditoria e2e (2026-09-08): sin la limpieza periodica,
    // el mapa interno crecia sin limite -- una entrada por cada email que
    // fallara un login, para siempre, aunque la ventana de bloqueo ya
    // hubiera expirado.
    @Test
    public void limpiarExpirados_eliminaEntradasCuyaVentanaYaExpiro() {
        InMemoryLoginRateLimiter limiter = new InMemoryLoginRateLimiter();
        limiter.registrarFallo("viejo@medconnect.com");
        assertEquals(1, limiter.cantidadDeEntradas());

        limiter.limpiarExpirados(Instant.now().plus(16, ChronoUnit.MINUTES));

        assertEquals(0, limiter.cantidadDeEntradas());
    }

    @Test
    public void limpiarExpirados_noTocaEntradasQueTodaviaEstanBloqueando() {
        InMemoryLoginRateLimiter limiter = new InMemoryLoginRateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.registrarFallo("bloqueado@medconnect.com");
        }

        limiter.limpiarExpirados(Instant.now().plus(5, ChronoUnit.MINUTES));

        assertEquals(1, limiter.cantidadDeEntradas());
        assertThrows(DemasiadosIntentosException.class, () -> limiter.verificarPermitido("bloqueado@medconnect.com"));
    }
}
