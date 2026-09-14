package com.medconnect.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

// Value Object: encapsula el formato de un DNI (antes era un String suelto
// en Paciente, sin validacion de formato -- solo se chequeaba que no
// estuviera vacio). Inmutable, igualdad por valor.
public final class Dni {

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): el regex
    // original ("^\d+$") aceptaba cualquier cantidad de digitos sin limite
    // -- "1" o un DNI de 50 digitos pasaban la validacion igual que uno
    // real. Acotado al rango real de DNI argentino (7 u 8 digitos).
    private static final Pattern FORMATO = Pattern.compile("^\\d{7,8}$");

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
