# Reanudar trabajo — MedConnect

Este archivo se mantiene actualizado al final de cada sesión de trabajo para que,
aunque pasen días sin conectarte, se pueda seguir sin releer el proyecto entero.
Última actualización: **2026-08-22**, tras mergear PR #17 (restricción por
rol: qué puede hacer cada uno de ADMINISTRADOR/MEDICO/PACIENTE).

## Stack y arquitectura

- **Backend**: Spring Boot 3.2 / Java 17, Maven (`backend/`).
- **Persistencia**: JPA + **PostgreSQL real** (no H2, no Docker). Requiere Postgres
  instalado nativamente en Windows, DB `medconnect`, user `postgres`.
  `spring.jpa.hibernate.ddl-auto=update` crea/actualiza las tablas solo.
- **Seguridad**: JWT stateless (HS512, expira a las 24hs, `jwt.secret`/`jwt.expiration-ms`
  en `application.properties`, override por env var `JWT_SECRET`/`JWT_EXPIRATION_MS`
  igual que `DB_PASSWORD`). `/api/auth/**` (`registro`, `login`) es lo único
  público; el resto de `/api/**` requiere `Authorization: Bearer <token>` válido,
  **y desde el PR #17 además está restringido por rol** (ver bloque dedicado
  más abajo). `Usuario` (email/contrasena hasheada con BCrypt/role) es la
  identidad de login, separada de Médico/Paciente a propósito (el campo
  `contrasena` suelto que tenía `Medico` desde el primer PR sigue ahí sin
  usar, no se tocó). `JwtAuthenticationFilter` (`infrastructure/security/`)
  puebla el `SecurityContext`; `JwtTokenService` firma/valida. CSRF sigue
  deshabilitado (no aplica con JWT).
- **Frontend**: React 18 + Vite 5 (`frontend/`), `fetch` plano (sin axios). Sin
  sesión válida se muestra `LoginScreen` (login + link a registro) en vez de
  la app; el JWT se guarda en `localStorage` (`medconnect_auth`, incluye
  token/id/nombre/email/role) y se manda en cada request vía el helper
  `apiFetch` — una respuesta **401** (token inválido/expirado) desloguea sola
  y vuelve al login. **Ojo**: un 403 (acción prohibida por rol) *no* debe
  desloguear — es un error de permisos, no de sesión; ese bug existió hasta
  el PR #17 y desconectaba a cualquier MEDICO/PACIENTE al primer intento de
  una acción restringida.
- **Estilos**: Tailwind CSS v3. Header/botones en teal (`primary`), texto en
  slate (`neutral`), y las **cards en tono "papel" cálido** (paleta custom
  `paper` en `tailwind.config.js`, usada en `.card` y en las franjas de
  encabezado/hover de las tablas) sobre un fondo de página gris-azulado
  (`bg-neutral-200`) — decisión explícita del usuario: nada de blanco puro,
  "rompe los ojos". Contrato de clases reutilizables en `frontend/src/index.css`
  (`.card`, `.heading`, `.label`, `.input-field`, `.btn-primary`, `.btn-secondary`,
  `.badge*`). **Usar siempre estas clases** para mantener coherencia visual, no
  clases Tailwind sueltas para botones/inputs/cards. Los inputs de texto con
  placeholder (login, registro, forms de Médico/Paciente) usan el componente
  `FloatingInput` (label flota arriba al enfocar o si ya hay valor cargado,
  estilo Material) — **para un input de texto nuevo, usar `FloatingInput` en
  vez de `<input placeholder=...>` suelto**, salvo que ya tenga un `<label>`
  fijo arriba (como los filtros de Turnos). Los botones (`.btn` en
  `index.css`) tienen efecto ripple Material (listener global de `mousedown`
  en `App`, busca `.btn-primary, .btn-secondary` — **ojo, `.btn` solo no
  existe en el DOM**, Tailwind lo inlinea vía `@apply` dentro de esas dos
  clases) más sombra/elevación al hover y `active:scale-[0.97]`.
- **Validación y errores**: los mensajes nativos del navegador (`Please fill
  out this field`, etc.) están traducidos al español vía `handleInvalid`/
  `clearValidity` (Constraint Validation API, `el.setCustomValidity(...)`),
  ya wireados en `FloatingInput` y en los `<select>` obligatorios de "Crear
  turno" — **si agregás un input/select requerido nuevo, hay que cablearlos
  ahí también o el navegador muestra el mensaje en inglés**. Todos los
  errores/éxitos (login, registro, Médico, Paciente, Turnos) se muestran con
  un sistema de toasts (`notify(mensaje, tipo)` en `App`, tipo `'error'` por
  default o `'success'`) en vez de banners inline — no quedan estados locales
  de error por componente. **Importante**: el backend devuelve los errores
  como texto plano (`ResponseEntity<String>`), no JSON — usar siempre el
  helper `readErrorMessage(resp)` para leerlos (hace `resp.text()` primero),
  nunca `resp.json().catch(() => null)`, que falla silenciosamente y deja un
  `HTTP 401` genérico en vez del mensaje real. Las tablas (Médicos,
  Pacientes, Turnos) muestran `SkeletonRows` mientras cargan por primera vez
  (guardado con `xLoading && x.length === 0`, no solo `xLoading`, para que no
  parpadee en cada refresco tras una acción).
- **Restricción por rol** (PR #17): `ADMINISTRADOR` gestiona todo (CRUD
  médicos/pacientes/turnos). `MEDICO` puede ver médicos/pacientes y
  crear/confirmar/cancelar turnos, pero no crear/editar/eliminar médicos ni
  pacientes. `PACIENTE` solo puede ver (ningún POST/PUT/DELETE/PATCH). Es
  **restricción global por operación, no por dueño del dato** — no existe un
  "MEDICO solo ve sus propios turnos" porque `Usuario` (login) no está
  vinculado a `Medico`/`Paciente` en el modelo; si se pide esa restricción
  más fina hace falta agregar esa relación primero (ver "huecos conocidos").
  Se implementa 100% en `SecurityConfig` con `requestMatchers(HttpMethod.X,
  "/api/...").hasRole(...)`/`hasAnyRole(...)`, sin tocar dominio ni casos de
  uso — **si agregás un endpoint de escritura nuevo, hay que sumarle su
  regla ahí, si no queda abierto a cualquier usuario autenticado**. En el
  frontend, `esAdmin`/`puedeGestionarTurnos` (calculados de `auth.role` en
  `App`) ocultan los forms/botones que el backend ya no permite — **si
  agregás una acción nueva restringida por rol, hay que ocultarla en el
  frontend además de bloquearla en el backend**, o el usuario ve un botón
  que siempre falla.
- **Arquitectura backend**: hexagonal por entidad, calcada tres veces (Turno,
  Médico, Paciente):
  - `domain/model/` — clase de dominio plana (POJO)
  - `domain/port/<X>Repository.java` — interfaz del puerto
  - `domain/exception/<X>InvalidoException.java` — validación de negocio
  - `application/usecase/Crear<X>UseCase|Service.java` y `Buscar<X>UseCase|Service.java`
    (+ `Create<X>Request/Response`)
  - `infrastructure/persistence/<X>Entity.java` + `<X>JpaRepository.java` +
    `<X>RepositoryAdapter.java` (`@Repository @Profile("!test")`)
  - `interfaces/rest/<X>Controller.java` + `<X>Request/Response.java`
  - Errores de validación → `GlobalExceptionHandler` (un `@ExceptionHandler` por excepción, devuelve 400 con el mensaje)
  - Médico y Paciente además tienen `Actualizar<X>UseCase|Service` (`PUT /{id}`,
    reutiliza `Create<X>Request`) y `Eliminar<X>UseCase|Service` (`DELETE /{id}`).
    **Eliminar es siempre soft delete**: el adapter JPA tiene un campo `activo`
    (Boolean, no boolean primitivo — para no romper filas viejas sin esa
    columna) en el `Entity`, con queries `findActivoById`/`findAllActivos` que
    tratan `activo IS NULL` como activo. `eliminar()` marca `activo=false` en
    vez de borrar la fila (los turnos ya creados no quedan huérfanos). El
    dominio y los casos de uso no saben que es soft delete, es un detalle
    100% de `infrastructure/persistence`.

  **Si agregás una entidad nueva, copiá este patrón exacto** — no inventar uno nuevo.

## Qué está implementado (PRs #1–#14, todos mergeados a `develop`)

- **Auth**: `POST /api/auth/registro` (nombre/email/contraseña ≥6 caracteres/role,
  rechaza email duplicado), `POST /api/auth/login` (devuelve JWT + datos del
  usuario, 401 si las credenciales son incorrectas). Ver "Seguridad" arriba.
- **Turno**: crear (`POST /api/turnos`, valida solapamiento por médico+fecha,
  y que `medicoId`/`pacienteId` existan realmente), buscar por id
  (`GET /api/turnos/{id}` → 404 si no existe), listar con filtro opcional por
  `medicoId` o `pacienteId` (`GET /api/turnos?medicoId=..&pacienteId=..`),
  cambiar estado (`PATCH /api/turnos/{id}/estado`, body `{"estado":"CONFIRMADO"}`
  o `"CANCELADO"`; un turno `CANCELADO` no puede volver a modificarse → 400;
  estado inválido en el body → 400; id inexistente → 404).
- **Médico**: crear (`POST /api/medicos`, valida nombre/especialidad/matrícula
  obligatorios), listar (`GET /api/medicos`), buscar por id (`GET /api/medicos/{id}`),
  editar (`PUT /api/medicos/{id}`), eliminar (`DELETE /api/medicos/{id}`, soft delete).
- **Paciente**: crear (`POST /api/pacientes`, valida nombre/dni obligatorios;
  campos opcionales: `telefono`, `direccion`, `obraSocial`, `numeroAfiliado`,
  `plan`, `email` — la obra social sola no alcanza, dos personas con la misma
  prepaga pueden tener plan y número de afiliado distintos), listar
  (`GET /api/pacientes`), buscar por id (`GET /api/pacientes/{id}`), editar
  (`PUT /api/pacientes/{id}`), eliminar (`DELETE /api/pacientes/{id}`, soft delete).
- **Frontend** (`frontend/src/App.jsx`, un solo archivo):
  - Sección "Médicos": un solo form sirve para alta y edición (botón "Editar"
    por fila precarga los datos, cambia a "Guardar cambios" y hace PUT; botón
    "Cancelar" vuelve a modo alta). Botón "Eliminar" pide confirmación (`window.confirm`).
    Ambos forms incluyen `direccion` (existía en el backend desde el principio
    pero no estaba expuesta en la UI hasta el PR #13 — antes de asumir que un
    campo "falta", conviene revisar si ya está en el modelo/API y solo no se
    muestra en el frontend).
  - Sección "Pacientes": mismo patrón de alta/edición/eliminación.
  - Sección "Crear turno": selects de médico/paciente (poblados desde las APIs
    de arriba, ya no son inputs numéricos de ID).
  - Sección "Turnos": tabla con auto-carga al abrir, refresco automático tras
    crear un turno, filtro por médico ID / paciente ID, botón "Ver todos".
    Resuelve nombre de médico/paciente por id (fallback a `#id` si no está en
    la lista cargada, p. ej. turnos de prueba con IDs que ya no existen).
    Columna "Acciones" con botón Confirmar (solo si `PENDIENTE`) y Cancelar
    (si no está ya `CANCELADO`), llaman al `PATCH .../estado` y refrescan.
- Tests: 65 tests (unitarios de casos de uso + MockMvc de controllers +
  integración end-to-end con repos fake en memoria vía `@Profile("test")`).
  Todos verificados también contra Postgres real con curl y en navegador real
  con Playwright (no solo tests automatizados).

## Qué NO está implementado todavía (huecos conocidos)

- Restricción por **datos propios**: la restricción por rol (PR #17) es
  global por operación, no filtra por dueño del dato — un `MEDICO` ve todos
  los turnos, no solo los suyos, y lo mismo un `PACIENTE`. Para eso hace
  falta vincular `Usuario` con `Medico`/`Paciente` (no existe esa relación
  hoy), que es un cambio de modelo más grande — no se hizo porque no se pidió
  todavía.
- Paginación en los listados (`buscarTodos()` trae todo).
- El `TurnoRepositoryAdapter` sigue reconstruyendo `Medico`/`Paciente` como
  objetos "solo id" a partir del id crudo (no trae nombre/especialidad); no es
  un problema hoy porque `TurnoResponse` solo expone los ids y el frontend
  resuelve el nombre por su cuenta desde `/api/medicos` y `/api/pacientes`.

## Cómo levantar todo (Windows / PowerShell o Git Bash)

### Backend
Necesita Postgres 17 **nativo** corriendo en `localhost:5432`, DB `medconnect`,
password `postgres123` (o el que hayas configurado — mirá si cambió).

```bash
cd backend
DB_PASSWORD=postgres123 ./mvnw.cmd spring-boot:run   # sirve en :8080
# o en PowerShell: $env:DB_PASSWORD='postgres123'; .\mvnw.cmd spring-boot:run
```
**Ojo**: el usuario tiene contenedores Docker de otros proyectos (`capymeal-*`,
`campus-*`, etc.) que a veces quedan corriendo y ocupan justo `:8080` (y
`:5173`/`:5174`, ver más abajo) — `netstat -ano | grep LISTEN` + `tasklist //FI
"PID eq <pid>"` identifica si es Docker (`com.docker.backend.exe`) y `docker ps`
muestra qué contenedor específico tiene el mapeo. **No los toques** (son
proyectos del usuario corriendo aparte) — si hace falta levantar el backend de
MedConnect igual, usar `SERVER_PORT=8090` (o el que esté libre) solo para esa
sesión de prueba, sin commitear ningún cambio de puerto en `App.jsx` (las URLs
ahí están hardcodeadas a `:8080` a propósito).

```bash
./mvnw.cmd test   # 65 tests, no necesita Postgres levantado (usa fakes en memoria)
```

### Frontend
```bash
cd frontend
npm install   # solo si no están instaladas las deps
npm run dev
```
**Ojo**: `5173` (proyecto "Campus") y `5174` (contenedor Docker
`capymeal-frontend`) pueden estar ambos ocupados por otros proyectos del
usuario — Vite cae solo al siguiente puerto libre (esta sesión terminó en
`5175`), así que **no asumas el puerto, mirá el log de `npm run dev`** para
confirmar el real antes de probar en el navegador o de apuntar un script de
Playwright.

### Probar endpoints (ejemplos)
`/api/**` ahora requiere JWT (salvo `/api/auth/**`). Ojo con tildes en `curl -d`
en Git Bash: mangla el UTF-8 y da un 400/403 que parece un bug de seguridad
pero no lo es — usar `--data-binary @archivo.json` con un archivo en vez de
`-d '...'` inline si el body tiene tildes/ñ.

```bash
# registro (una sola vez) + login
curl -X POST http://localhost:8080/api/auth/registro -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Perez","email":"ana@medconnect.com","contrasena":"secreto123","role":"ADMINISTRADOR"}'

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"ana@medconnect.com","contrasena":"secreto123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# el resto de /api/** necesita el header Authorization
curl -X POST http://localhost:8080/api/medicos -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"nombre":"Ana Perez","especialidad":"Cardiologia","matricula":"MP1234"}'

curl -X POST http://localhost:8080/api/pacientes -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"nombre":"Juan Gomez","dni":"30111222"}'

curl -X POST http://localhost:8080/api/turnos -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"fechaHora":"2026-08-12T10:00:00","especialidad":"Cardiologia","medicoId":1,"pacienteId":1}'

curl http://localhost:8080/api/turnos -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/api/medicos -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/api/pacientes -H "Authorization: Bearer $TOKEN"
```

## Convenciones de trabajo confirmadas con el usuario

- **Git flow**: rama `feature/<nombre>` desde `develop` → commits atómicos por
  concern lógico (ej: backend Médico, backend Paciente, frontend, cada uno en
  su propio commit aunque toquen el mismo archivo compartido) → push → PR a
  `develop` con resumen + test plan → **esperar a que el usuario mergee él
  mismo** (nunca mergear por mi cuenta) → sync (`checkout develop`, `pull`,
  borrar rama local y remota).
- No usar `git add -A`/`git add .`; agregar archivos por nombre explícito.
- Antes de cada commit, revisar que no haya secretos en el diff
  (`git diff --cached`), especialmente en `.claude/settings.json` /
  `.claude/settings.local.json` (personal, gitignored) — ya pasó una vez que
  la password de Postgres quedó en un patrón de permiso ahí.
- `.claude/settings.json` se auto-modifica solo (agrega patrones de permisos
  nuevos) mientras trabajo — si bloquea un `git checkout`, es seguro hacer
  `git stash push -- .claude/settings.json` y dropear el stash después, el
  contenido siempre es benigno (nunca secretos, ya se verificó varias veces).
- Verificar siempre contra el sistema real, no solo tests: backend con curl
  contra Postgres real, frontend en navegador real (Playwright, porque
  `chromium-cli` no está disponible en este entorno — hay un driver ad-hoc en
  el scratchpad de la sesión, se recrea si hace falta).
- Estilos: seguir el contrato de clases de `frontend/src/index.css`, no
  clases Tailwind sueltas.
- Al terminar de probar en navegador, parar backend y frontend (no dejar
  procesos colgados).

## Historial de PRs (todos mergeados)

1. `feature/dominio-usuarios-turnos` — modelos de dominio base
2. `feature/turnos-rest` — endpoints REST iniciales de Turno
3. `fix/readme-restore`
4. `feature/buscar-turno` — `BuscarTurnoUseCase` + fix de build roto
5. `feature/persistencia-jpa-postgres` — persistencia JPA real + `SecurityConfig`
6. `feature/frontend-listado-turnos` — tabla de turnos, filtros, fix de bug de
   stale closure en botón "Ver todos"; + fix de seguridad (password expuesta
   en `.claude/settings.json`)
7. `feature/frontend-tailwind-design-system` — Tailwind + paleta + contrato de clases
8. `feature/gestion-medicos-pacientes` (PR #9) — CRUD de Médico y Paciente +
   selects en el alta de turnos
9. `feature/ciclo-vida-turno` (PR #10) — validar que médico/paciente existan al
   crear un turno + `PATCH /api/turnos/{id}/estado` (confirmar/cancelar) +
   botones en el frontend
10. `feature/paciente-obra-social-plan` (PR #11) — `numeroAfiliado`/`plan` en
    Paciente + rediseño de fondo y cards a tonos cálidos ("papel envejecido")
11. `feature/editar-eliminar-medico-paciente` (PR #12) — `PUT`/`DELETE` para
    Médico y Paciente, eliminar es soft delete (`activo=false`), edición y
    borrado con confirmación en el frontend
12. `feature/direccion-medico-paciente-frontend` (PR #13) — exponer el campo
    `direccion` (ya existía en el backend) en los forms y tablas del frontend
13. `feature/autenticacion-jwt` (PR #14) — login/registro con JWT, `/api/**`
    detrás de auth (sin restricción por rol todavía), + labels flotantes
    (`FloatingInput`) en los inputs de texto con placeholder
14. `feature/material-ripple-elevacion` (PR #15) — efecto ripple y elevación
    Material en los botones
15. `feature/toasts-skeletons-mensajes-es` (PR #16) — mensajes de validación
    nativa en español, sistema de toasts, skeleton loaders y fix del parseo
    de errores del backend (texto plano, no JSON). Nota: esta PR se armó a
    partir de commits que habían quedado huérfanos localmente (nunca se
    pushearon antes de mergear y borrar la rama anterior) — recuperados vía
    `git reflog`/`git fsck --dangling` y rebaseados sobre `develop`. Desde
    entonces, cada commit se pushea enseguida en vez de acumularlos.
16. `feature/restriccion-por-rol` (PR #17) — restricción por rol en el backend
    (`SecurityConfig`, ver bloque dedicado arriba) + ocultar en el frontend
    las acciones que cada rol no puede usar + fix de un bug real (`apiFetch`
    deslogueaba también ante un 403, no solo 401).

## Siguientes pasos sugeridos (sin decidir aún — preguntale al usuario)

- Restricción por datos propios (ver "huecos conocidos" arriba) — requiere
  vincular `Usuario` con `Medico`/`Paciente`, más grande que el PR #17.
- Paginación en los listados si el volumen de datos crece.
- Editar/cancelar turno ya existe (PR #10); falta eliminar un turno del todo
  si alguna vez hace falta (hoy solo se puede cancelar, que es lo correcto
  para no perder historial — probablemente no haga falta un DELETE real).
