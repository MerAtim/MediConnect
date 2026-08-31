package com.medconnect.infrastructure.security;

import com.medconnect.application.usecase.LoginRateLimiter;
import com.medconnect.domain.exception.DemasiadosIntentosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

// Limita intentos fallidos de login por email para frenar fuerza bruta.
// Es un contador en memoria por instancia: se resetea si el proceso
// reinicia y no se comparte entre instancias si el backend corre
// replicado — suficiente para el alcance actual del proyecto (una sola
// instancia), habria que pasar a algo compartido (ej. Redis) si eso cambia.
@Component
public class InMemoryLoginRateLimiter implements LoginRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(InMemoryLoginRateLimiter.class);

    private static final int MAX_INTENTOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Intentos> intentosPorEmail = new ConcurrentHashMap<>();

    @Override
    public void verificarPermitido(String email) {
        Intentos intentos = intentosPorEmail.get(normalizar(email));
        if (intentos != null && intentos.excedioLimite()) {
            log.warn("Email bloqueado por demasiados intentos fallidos de login: email={}", normalizar(email));
            throw new DemasiadosIntentosException("Demasiados intentos fallidos. Probá de nuevo en unos minutos.");
        }
    }

    @Override
    public void registrarFallo(String email) {
        intentosPorEmail.compute(normalizar(email), (key, actual) ->
                (actual == null || actual.ventanaExpirada()) ? new Intentos(1) : actual.incrementar());
    }

    @Override
    public void registrarExito(String email) {
        intentosPorEmail.remove(normalizar(email));
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static final class Intentos {
        private final int cantidad;
        private final Instant primerIntento;

        Intentos(int cantidad) {
            this.cantidad = cantidad;
            this.primerIntento = Instant.now();
        }

        private Intentos(int cantidad, Instant primerIntento) {
            this.cantidad = cantidad;
            this.primerIntento = primerIntento;
        }

        boolean excedioLimite() {
            return cantidad >= MAX_INTENTOS && !ventanaExpirada();
        }

        boolean ventanaExpirada() {
            return Instant.now().isAfter(primerIntento.plus(VENTANA));
        }

        Intentos incrementar() {
            return new Intentos(cantidad + 1, primerIntento);
        }
    }
}
