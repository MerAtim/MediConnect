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

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): estos tres
    // tests usaban Thread.sleep(5) para forzar que "emitidoEn" y el corte
    // interno (System.currentTimeMillis() dentro de revocarTokensPrevios)
    // cayeran en milisegundos distintos -- innecesariamente fragil en un
    // runner de reloj de baja resolucion o bajo carga, y hace la suite mas
    // lenta. En vez de dormir el hilo, se construye "emitidoEn" con un
    // offset explicito (+/-1000ms) relativo al reloj real, sin depender de
    // que pase tiempo de verdad entre una linea y la siguiente.
    @Test
    public void fueRevocado_esTrue_paraUnTokenEmitidoAntesDeLaRevocacion() {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);
        Date emitidoEn = new Date(System.currentTimeMillis() - 1000);

        service.revocarTokensPrevios("ana@medconnect.com");

        assertTrue(service.fueRevocado("ana@medconnect.com", emitidoEn));
    }

    @Test
    public void fueRevocado_esFalse_paraUnTokenEmitidoDespuesDeLaRevocacion() {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);

        service.revocarTokensPrevios("ana@medconnect.com");
        Date emitidoEn = new Date(System.currentTimeMillis() + 1000);

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
    public void normalizaEmailPorMayusculasYEspacios() {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService(EXPIRATION_MS);
        Date emitidoEn = new Date(System.currentTimeMillis() - 1000);

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
