package com.medconnect.domain.port;

import java.util.Map;

public interface EspecialidadDuracionRepository {

    // Decision de producto confirmada con el usuario: una especialidad sin
    // fila propia en especialidad_duraciones usa este default, no bloquea
    // la creacion de turnos. Vive aca (no hardcodeado en el adapter) porque
    // tanto el adapter como el use case lo necesitan.
    int DURACION_DEFAULT_MINUTOS = 30;

    Map<String, Integer> buscarTodas();

    int buscarDuracion(String especialidad);

    void establecer(String especialidad, int minutos);
}
