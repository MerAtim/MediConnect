package com.medconnect;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): construccion
// repetida y verbosa de "new Medico(id, null, null, null, null, null, null)"
// / "new Paciente(id, null, null, null, null, null, null, null, null)" con
// listas largas de null posicionales, repetida 60+ veces combinadas en
// tests que solo necesitan el id (para relacionar un Turno/RegistroClinico
// con su medico/paciente, no para probar nada de esos campos). Delega en
// Medico.conId()/Paciente.conId() (mismo patron, ahora tambien usado por el
// codigo de produccion -- ver el comentario en Medico.java) para que exista
// una sola fuente de verdad de "como se ve una referencia liviana", no dos
// implementaciones independientes del mismo concepto.
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Medico medicoConId(long id) {
        return Medico.conId(id);
    }

    public static Paciente pacienteConId(long id) {
        return Paciente.conId(id);
    }
}
