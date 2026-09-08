package com.medconnect.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

// Value Object: encapsula el formato de un email (antes era un String suelto
// repetido en Medico/Paciente/Usuario, sin validacion de formato en ningun
// lado salvo un regex duplicado a mano en RegistrarUsuarioService). Inmutable,
// igualdad por valor.
public final class Email {

    private static final Pattern FORMATO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String valor;

    public Email(String valor) {
        if (!esFormatoValido(valor)) {
            throw new IllegalArgumentException("email invalido: " + valor);
        }
        this.valor = valor;
    }

    public static boolean esFormatoValido(String valor) {
        return valor != null && FORMATO.matcher(valor).matches();
    }

    // Medico/Paciente pueden no tener cuenta vinculada todavia ("sin
    // vincular" en el frontend llega como string vacio) -- a diferencia de
    // Usuario, donde el email siempre es obligatorio.
    public static Email deNullable(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return new Email(valor.trim());
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email other)) return false;
        return valor.equals(other.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
