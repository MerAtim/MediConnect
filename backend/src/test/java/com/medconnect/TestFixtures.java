package com.medconnect;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): construccion
// repetida y verbosa de "new Medico(id, null, null, null, null, null, null)"
// / "new Paciente(id, null, null, null, null, null, null, null, null)" con
// listas largas de null posicionales, repetida 60+ veces combinadas en
// tests que solo necesitan el id (para relacionar un Turno/RegistroClinico
// con su medico/paciente, no para probar nada de esos campos). Fragil ante
// cambios de firma del constructor -- cualquier campo nuevo obliga a tocar
// decenas de lineas sin relacion con lo que el test verifica.
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Medico medicoConId(long id) {
        return new Medico(id, null, null, null, null, null, null);
    }

    public static Paciente pacienteConId(long id) {
        return new Paciente(id, null, null, null, null, null, null, null, null);
    }
}
