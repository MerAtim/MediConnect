package com.medconnect.application.usecase;

import java.util.Date;

// MEDIUM de la re-auditoria e2e (2026-09-08): "sin revocacion de JWT" -- el
// logout solo borraba la cookie del lado del navegador; el token en si
// seguia siendo valido hasta que expiraba solo (24hs por defecto), sin
// forma de invalidarlo del lado del servidor ante un logout, un cambio de
// contrasena, o un token robado.
//
// No se trackea cada token individual (no hay jti): se marca, por email, un
// punto de corte en el tiempo -- cualquier token emitido antes de ese
// instante para ese email deja de ser valido, sin importar que tan reciente
// sea su expiracion. Efecto secundario aceptado: revocar invalida TODAS las
// sesiones activas de ese email (todos los dispositivos/navegadores), no
// una sola -- no hay jti por sesion para distinguirlas, y es consistente
// con "logout" y "cambio de contrasena" forzando un re-login limpio en
// todos lados.
public interface TokenRevocationService {

    void revocarTokensPrevios(String email);

    boolean fueRevocado(String email, Date emitidoEn);
}
