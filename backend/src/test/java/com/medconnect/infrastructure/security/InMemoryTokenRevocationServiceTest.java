package com.medconnect.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryTokenRevocationServiceTest {

    private static final long EXPIRATION_MS = 86_400_000L;

    @Test
    public void fueRevocado_esFalse_siNuncaSeRevoco() {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);

        assertFalse(service.fueRevocado("ana@medconnect.com", new Date()));
    }

    @Test
    public void fueRevocado_esTrue_paraUnTokenEmitidoAntesDeLaRevocacion() throws InterruptedException {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);
        Date emitidoEn = new Date();
        Thread.sleep(5);

        service.revocarTokensPrevios("ana@medconnect.com");

        assertTrue(service.fueRevocado("ana@medconnect.com", emitidoEn));
    }

    @Test
    public void fueRevocado_esFalse_paraUnTokenEmitidoDespuesDeLaRevocacion() throws InterruptedException {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);

        service.revocarTokensPrevios("ana@medconnect.com");
        Thread.sleep(5);
        Date emitidoEn = new Date();

        assertFalse(service.fueRevocado("ana@medconnect.com", emitidoEn));
    }

    @Test
    public void fueRevocado_noAfectaAOtroEmail() {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);
        Date emitidoEn = new Date();

        service.revocarTokensPrevios("ana@medconnect.com");

        assertFalse(service.fueRevocado("otro@medconnect.com", emitidoEn));
    }

    @Test
    public void normalizaEmailPorMayusculasYEspacios() throws InterruptedException {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);
        Date emitidoEn = new Date();
        Thread.sleep(5);

        service.revocarTokensPrevios(" Ana@Medconnect.com ");

        assertTrue(service.fueRevocado("ana@medconnect.com", emitidoEn));
    }

    // MEDIUM de la re-auditoria e2e (2026-09-08): sin esta limpieza, el mapa
    // de cortes por email crece sin limite igual que el de
    // InMemoryLoginRateLimiter -- ver limpiarCortesRedundantes().
    @Test
    public void limpiarCortesRedundantes_noRompeElChequeoDeUnCorteReciente() {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);
        service.revocarTokensPrevios("ana@medconnect.com");

        service.limpiarCortesRedundantes();

        // Un corte recien creado todavia esta dentro de la ventana de
        // expirationMs -- la limpieza no debe tocarlo.
        assertTrue(service.fueRevocado("ana@medconnect.com", new Date(0)));
    }
}
