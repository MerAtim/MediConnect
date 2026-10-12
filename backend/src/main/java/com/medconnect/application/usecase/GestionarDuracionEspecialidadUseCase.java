package com.medconnect.application.usecase;

import java.util.List;

public interface GestionarDuracionEspecialidadUseCase {

    // Combina las especialidades conocidas (derivadas de los medicos, mismo
    // criterio que MedicoController.especialidades()) con lo configurado en
    // especialidad_duraciones -- una especialidad conocida sin fila propia
    // aparece con el default, no queda afuera del listado.
    List<EspecialidadDuracion> listarTodas();

    void establecer(String especialidad, int minutos);
}
