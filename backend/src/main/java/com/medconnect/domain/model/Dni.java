package com.medconnect.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

// Value Object: encapsula el formato de un DNI (antes era un String suelto
// en Paciente, sin validacion de formato -- solo se chequeaba que no
// estuviera vacio). Inmutable, igualdad por valor.
public final class Dni {

    private static final Pattern FORMATO = Pattern.compile("^\\d+$");

    private final String valor;

    public Dni(String valor) {
        if (!esFormatoValido(valor)) {
            throw new IllegalArgumentException("dni invalido: " + valor);
        }
        this.valor = valor;
    }

    public static boolean esFormatoValido(String valor) {
        return valor != null && FORMATO.matcher(valor).matches();
    }

    // Los tests construyen Paciente como fixture liviano sin dni real (les
    // interesa solo el id), y el dominio no fuerza por si solo que un
    // Paciente tenga dni -- esa obligatoriedad la exige
    // CreatePacienteRequest.validar() al crear/editar via la API.
    public static Dni deNullable(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return new Dni(valor.trim());
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dni other)) return false;
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
