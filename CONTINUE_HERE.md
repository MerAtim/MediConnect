# Reanudar trabajo — MedConnect

Este archivo se mantiene actualizado al final de cada sesión de trabajo para que,
aunque pasen días sin conectarte, se pueda seguir sin releer el proyecto entero.
Última actualización: **2026-08-23**, tras mergear PR #19: historia clínica
compartida + un rediseño grande del flujo de turnos (quién puede crear/ver
qué, según rol — ver "Restricción por rol" más abajo).

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
  en `App`, busca `.btn-primary, .btn-secondary, .btn-danger` — **ojo, `.btn`
  solo no existe en el DOM**, Tailwind lo inlinea vía `@apply` dentro de esas
  clases) más sombra/elevación al hover y `active:scale-[0.97]`. Para
  confirmaciones destructivas (ej. un paciente cancelando su turno) **no usar
  `window.confirm`** — hay un componente `ConfirmModal` (mismo `.card`,
  overlay `bg-neutral-900/40`) que sigue el design system; `.btn-danger`
  (rojo, `bg-danger-600`) es el estilo para su botón de confirmar cuando la
  acción es irreversible. Si hace falta doble confirmación ("por las
  dudas"), encadenar dos `ConfirmModal` con un estado de "paso" (ver
  `pasoCancelacion` en `App` para el patrón).
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
- **Restricción por rol** (PR #17, ampliada bastante en `feature/historia-clinica`):
  ya no es solo "quién puede escribir", ahora también filtra **qué ve cada
  quien**, por dueño del dato:
  - `ADMINISTRADOR`: gestiona médicos y pacientes (CRUD completo, incluida la
    lista completa de ambos), y es el **único que puede crear turnos**
    (`POST /api/turnos` — antes también podía `MEDICO`, ya no: otorgar
    turnos es tarea de recepción/admin). No navega la historia clínica
    (ver bullet dedicado), solo la exporta.
  - `MEDICO`: **no ve el listado de médicos** (`GET /api/medicos` → 403 para
    este rol) ni la lista completa de pacientes — `GET /api/pacientes` le
    devuelve **solo los pacientes con los que tiene al menos un turno**
    (`PacienteController.buscarTodos()` resuelve el médico logueado por
    email y cruza contra sus turnos). Tampoco crea turnos. Sí puede
    confirmar/cancelar turnos y usar la historia clínica (crear/listar).
  - `PACIENTE`: no ve médicos ni pacientes, no crea turnos, **no puede
    confirmarlos** — solo puede **cancelar los suyos** (`PATCH
    .../estado` con `estado=CANCELADO`, rechazado con 400 si pide otro
    estado, y con 403 si el turno no es suyo). Ve únicamente sus propios
    turnos.
  - **Cómo se resuelve "de quién es este usuario logueado"**: `Medico` y
    `Paciente` ya tenían un campo `email` opcional (se completa a mano desde
    el form de alta/edición, sección Médicos/Pacientes, solo `esAdmin`). Si
    ese email coincide con el email de login (`Usuario.email`), quedan
    "vinculados" — `MedicoRepository.buscarPorEmail`/
    `PacienteRepository.buscarPorEmail` (nuevos) resuelven esa identidad en
    cada request vía `SecurityContextHolder.getContext().getAuthentication().getName()`
    (el filtro JWT ya guarda el email ahí, ver `JwtAuthenticationFilter`).
    **Si un médico/paciente no tiene el email cargado o no coincide, el
    filtro por rol le devuelve todo vacío** (no cae a "ver todo" — falla
    cerrado) — si un usuario de prueba ve listas vacías que no deberían
    estarlo, lo primero a revisar es si su `Medico`/`Paciente` tiene el
    email seteado igual al de su cuenta de login.
  - Se implementa mitad en `SecurityConfig` (`requestMatchers(HttpMethod.X,
    "/api/...").hasRole(...)`) para bloqueos duros por método, mitad **dentro
    de los controllers** (`TurnoController`, `PacienteController`) para el
    filtrado por dueño del dato, que no se puede expresar como regla de
    path/método — **si agregás un endpoint de escritura o listado nuevo,
    hay que decidir las dos cosas**: quién puede pegarle (SecurityConfig) y
    si necesita filtrarse por dueño (lógica en el controller).
  - En el frontend, `esAdmin`/`esMedico`/`esPaciente`/`puedeGestionarTurnos`
    (calculados de `auth.role` en `App`) ocultan secciones/botones enteros
    que el backend ya no permite — **si agregás una acción nueva restringida
    por rol, hay que ocultarla en el frontend además de bloquearla en el
    backend**, o el usuario ve un botón que siempre falla.
- **Historia clínica** (`RegistroClinico`, `feature/historia-clinica`): un
  registro clínico por consulta (diagnóstico/tratamiento/observaciones),
  vinculado a un médico y un paciente. La "historia clínica" de un paciente
  **no es una entidad separada** — es simplemente la lista de sus
  `RegistroClinico` ordenada por fecha desc (`GET /api/historias-clinicas
  ?pacienteId=X`); no hace falta "crearla" al dar de alta un paciente, existe
  vacía desde que el paciente existe. Es **append-only**: solo hay crear y
  listar, no editar ni eliminar (a propósito — es un registro de auditoría
  médica). Reglas de permiso, **distintas del resto de la app**:
  - `MEDICO`: puede crear (`POST /api/historias-clinicas`) y navegar la lista
    (`GET`) de cualquier paciente — con una validación: solo si ese médico
    tiene al menos un turno con ese paciente (`CrearRegistroClinicoService`
    cruza contra `TurnoRepository.buscarPorMedico`). El `medicoId` se manda
    explícito en el body (elegido de un `<select>`, o directamente el de la
    fila de turno expandida en el frontend), **no se resuelve del login por
    email** aunque esa vinculación ya exista para otras cosas (ver bullet de
    "Restricción por rol") — se mantuvo así por consistencia con
    `CrearTurnoService`, que ya funcionaba igual. En la práctica ya no
    importa mucho: como `GET /api/turnos` para `MEDICO` ahora solo devuelve
    sus propios turnos, el panel de historia clínica que arma esa fila solo
    puede pasar su propio `medicoId`.
  - `ADMINISTRADOR`: **no** puede navegar la lista (`GET` le da 403) — solo
    puede descargarla como archivo de texto (`GET
    /api/historias-clinicas/exportar?pacienteId=X`, `Content-Disposition:
    attachment`). Pensado para cuando un paciente la pide en recepción: el
    admin la descarga y se la entrega, pero no la mira desde la app.
  - `PACIENTE`: sin acceso a nada de esto (ni crear, ni ver, ni descargar) —
    la pide en persona en recepción.
  En el frontend, el botón "Ver historia" (que despliega los registros
  previos + el form para agregar uno nuevo) vive **dentro de la fila del
  turno correspondiente** en la sección Turnos, visible solo con `esMedico`
  (no `puedeGestionarTurnos`, que también incluye a ADMINISTRADOR); el botón
  "Descargar historia" vive en la fila del paciente en la sección Pacientes,
  visible solo con `esAdmin`.
  **Gotcha de test**: `CrearTurnoIntegrationTest` carga el `ApplicationContext`
  completo, así que cualquier puerto de repositorio nuevo (`XRepository`)
  necesita un bean fake (`InMemoryXRepository`) en su `TestConfig` interno o
  el contexto no levanta — no lo olvides si agregás otra entidad.
- **`Turno.preparacion`** (campo opcional, texto libre): instrucciones para
  el paciente antes de la consulta (ej. "Asistir 15 minutos antes y pasar
  por recepción para dar presente", o ayuno para un laboratorio). Se carga
  al otorgar el turno (sección "Otorgar turno", solo `esAdmin`) y se
  muestra en la tabla de Turnos para todos los roles que la ven, incluido
  el paciente.
- **`TurnoResponse`/`RegistroClinicoResponse` traen nombres embebidos**
  (`medicoNombre`, `medicoEspecialidad`, `pacienteNombre` — resueltos en el
  controller vía `BuscarMedicoUseCase`/`BuscarPacienteUseCase`, no en el
  frontend). Esto **no es un capricho**: `MEDICO` y `PACIENTE` ya no tienen
  acceso a `GET /api/medicos` (y `PACIENTE` tampoco a `GET /api/pacientes`),
  así que el frontend no puede resolver esos nombres por su cuenta con un
  `medicos.find(...)` como antes — **si un endpoint nuevo devuelve
  medicoId/pacienteId y lo va a ver alguien que no sea `ADMINISTRADOR`,
  hay que embeber el nombre en la respuesta, no asumir que el frontend
  puede resolverlo con las listas cargadas**.
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

## Qué está implementado (ver "Historial de PRs" más abajo para el detalle)

- **Auth**: `POST /api/auth/registro` (nombre/email/contraseña ≥6 caracteres/role,
  rechaza email duplicado), `POST /api/auth/login` (devuelve JWT + datos del
  usuario, 401 si las credenciales son incorrectas). Ver "Seguridad" arriba.
- **Turno**: crear (`POST /api/turnos`, **solo ADMINISTRADOR** desde
  `feature/historia-clinica` — antes también `MEDICO`, ver bullet de
  "Restricción por rol"; valida solapamiento por médico+fecha, que
  `medicoId`/`pacienteId` existan, y acepta `preparacion` opcional), buscar
  por id (`GET /api/turnos/{id}` → 404 si no existe, sin filtrar por dueño
  todavía — ver "huecos conocidos"), listar (`GET /api/turnos`, con
  `medicoId`/`pacienteId` como filtro opcional **solo para ADMINISTRADOR** —
  para `MEDICO`/`PACIENTE` esos parámetros se ignoran y siempre devuelve
  solo lo propio), cambiar estado (`PATCH /api/turnos/{id}/estado`, body
  `{"estado":"CONFIRMADO"}` o `"CANCELADO"`; `CANCELADO` no puede volver a
  modificarse → 400; estado inválido → 400; id inexistente → 404;
  `PACIENTE` **solo puede pedir `CANCELADO`** de su propio turno — otro
  estado → 400, turno ajeno → 403).
- **Médico**: crear/editar/eliminar (`POST`/`PUT`/`DELETE /api/medicos`,
  **solo ADMINISTRADOR**, igual que antes), listar/buscar por id (`GET
  /api/medicos`, `GET /api/medicos/{id}`, **solo ADMINISTRADOR** desde
  `feature/historia-clinica` — `MEDICO`/`PACIENTE` ya no navegan el
  directorio de médicos, reciben 403).
- **Paciente**: crear/editar/eliminar (`POST`/`PUT`/`DELETE /api/pacientes`,
  **solo ADMINISTRADOR**; campos opcionales: `telefono`, `direccion`,
  `obraSocial`, `numeroAfiliado`, `plan`, `email` — la obra social sola no
  alcanza, dos personas con la misma prepaga pueden tener plan y número de
  afiliado distintos). Listar (`GET /api/pacientes`): `ADMINISTRADOR` ve
  todos; `MEDICO` ve solo los que tienen un turno con él (ver bullet de
  "Restricción por rol"); `PACIENTE` no tiene acceso (403). Buscar por id
  (`GET /api/pacientes/{id}`) sigue abierto a cualquier autenticado, sin
  filtrar por dueño (mismo hueco que `GET /api/turnos/{id}`).
- **Historia clínica** (ver bullet dedicado arriba): crear (`POST
  /api/historias-clinicas`, MEDICO, valida que exista un turno entre ese
  médico y ese paciente), listar por paciente (`GET
  /api/historias-clinicas?pacienteId=X`, MEDICO), exportar como texto
  descargable (`GET /api/historias-clinicas/exportar?pacienteId=X`,
  ADMINISTRADOR, 404 si el paciente no existe).
- **Frontend** (`frontend/src/App.jsx`, un solo archivo):
  - Sección "Médicos" (**solo `esAdmin`**): un solo form sirve para alta y
    edición (botón "Editar" por fila precarga los datos, cambia a "Guardar
    cambios" y hace PUT; botón "Cancelar" vuelve a modo alta). Botón
    "Eliminar" pide confirmación (`window.confirm`). El form incluye
    `direccion` y `email` — este último es clave para la vinculación de
    identidad (ver "Restricción por rol"), no es solo un dato de contacto.
  - Sección "Pacientes" (**`esAdmin` o `esMedico`**, título cambia a "Mis
    pacientes" para `esMedico`): alta/edición/eliminación solo para
    `esAdmin` (`esMedico` la ve de solo lectura, ya filtrada por el
    backend a sus propios pacientes — la columna Acciones muestra "—").
    Para `esAdmin`, cada fila suma "Descargar historia" (exporta el `.txt`
    de ese paciente).
  - Sección "Otorgar turno" (**solo `esAdmin`**, antes se llamaba "Crear
    turno" y tenía selects planos de médico/paciente con todo el listado):
    ahora es un flujo de dos pasos — 1) buscar paciente por DNI (filtro
    client-side sobre `pacientes`, que `esAdmin` ya tiene cargado
    completo); 2) recién con un paciente encontrado, se muestra el resto
    del form: `<select>` de especialidad (opciones = especialidades
    distintas presentes en `medicos`), `<select>` de médico **filtrado por
    esa especialidad**, fecha/hora, y `preparacion` (texto libre opcional).
    Pensado para el caso real: "el paciente dice que necesita
    diabetología" → elegís la especialidad → el select de médico ya solo
    muestra los de esa especialidad.
  - Sección "Turnos" (título "Mis turnos" para `esPaciente`): tabla con
    auto-carga al abrir, refresco automático tras crear/cambiar un turno.
    El filtro por médico ID/paciente ID y el botón "Ver todos" son **solo
    para `esAdmin`** (para los otros roles el backend ya devuelve solo lo
    propio, filtrar por ID no tendría efecto). Nombres de médico/paciente
    y `preparacion` vienen **embebidos en la respuesta** (`t.medicoNombre`,
    etc.), no se resuelven más con un `.find()` sobre `medicos`/`pacientes`
    (ver bullet dedicado más arriba). Para `esMedico`, arriba de la tabla
    hay un banner grande "Turnos para hoy: DD/MM/AAAA" (pedido explícito:
    los médicos necesitan la fecha bien visible para hacer recetas).
    Columna "Acciones": `esAdmin`/`esMedico` ven Confirmar (si
    `PENDIENTE`)/Cancelar (si no `CANCELADO`) + "Ver historia" (solo
    `esMedico`, despliega una fila con la historia clínica de ese
    paciente); `esPaciente` ve solo "Cancelar" (si no está ya
    `CANCELADO`), que dispara un `ConfirmModal` **doble** (dos pasos, "por
    las dudas") en vez de `window.confirm` — ver `pasoCancelacion` y
    `ConfirmModal` en el bullet de Estilos.
- Tests: 97 tests (unitarios de casos de uso + MockMvc de controllers +
  integración end-to-end con repos fake en memoria vía `@Profile("test")`).
  Todos verificados también contra Postgres real con curl y en navegador real
  con Playwright (no solo tests automatizados).

## Qué NO está implementado todavía (huecos conocidos)

- Restricción por **datos propios**: en gran parte resuelta en
  `feature/historia-clinica` — `MEDICO` ya ve solo sus turnos/pacientes,
  `PACIENTE` ya ve solo los suyos (vinculación por email, ver bullet de
  "Restricción por rol"). Lo que **falta** todavía:
  - `GET /api/turnos/{id}` y `GET /api/pacientes/{id}` (buscar por id) NO
    filtran por dueño — cualquier autenticado puede pedir un id puntual que
    no sea suyo. No es explotable desde la UI actual (no se usan así), pero
    es un hueco real si se llama a la API directo.
  - La vinculación depende de que el `email` de `Medico`/`Paciente`
    coincida EXACTO con el de `Usuario` — es manual (lo carga `esAdmin` en
    el form), no hay UI para "vincular esta cuenta con este médico" más
    allá de escribir el mismo email en los dos lados.
  - Asume 1 email = 1 médico/paciente. No hay validación que lo fuerce.
- Paginación en los listados (`buscarTodos()` trae todo).
- `TurnoController.toResponse()` y `RegistroClinicoController.toResponse()`
  hacen una consulta extra por fila para resolver `medicoNombre`/
  `pacienteNombre` (N+1) — aceptable al volumen de datos actual, pero es lo
  primero a mirar si un listado se pone lento.
- ~~No hay ninguna forma legítima de crear un `ADMINISTRADOR`~~ — resuelto:
  `POST /api/usuarios` (solo `ADMINISTRADOR`, ver más abajo) permite dar de
  alta cualquier rol, incluido `ADMINISTRADOR`.

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
./mvnw.cmd test   # 97 tests, no necesita Postgres levantado (usa fakes en memoria)
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

**Ojo**: desde el PR #18 el registro público (`/api/auth/registro`) **no
acepta `role=ADMINISTRADOR`** (400) — para probar cosas de admin, loguearse
con la cuenta que ya existe en la base de pruebas
(`admin.rol@medconnect.com` / `secreto123`), no intentar registrar una nueva.

```bash
# login como admin (cuenta ya existente, ver arriba)
TOKEN_ADMIN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"admin.rol@medconnect.com","contrasena":"secreto123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# alta de médico/paciente: solo ADMINISTRADOR
curl -X POST http://localhost:8080/api/medicos -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN_ADMIN" \
  -d '{"nombre":"Ana Perez","especialidad":"Cardiologia","matricula":"MP1234","email":"medico.rol@medconnect.com"}'

curl -X POST http://localhost:8080/api/pacientes -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN_ADMIN" \
  -d '{"nombre":"Juan Gomez","dni":"30111222"}'

# crear turno: solo ADMINISTRADOR (preparacion es opcional)
curl -X POST http://localhost:8080/api/turnos -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN_ADMIN" \
  -d '{"fechaHora":"2026-08-12T10:00:00","especialidad":"Cardiologia","medicoId":1,"pacienteId":1,"preparacion":"Ayuno de 8 horas"}'

curl http://localhost:8080/api/turnos -H "Authorization: Bearer $TOKEN_ADMIN"
curl http://localhost:8080/api/medicos -H "Authorization: Bearer $TOKEN_ADMIN"
curl http://localhost:8080/api/pacientes -H "Authorization: Bearer $TOKEN_ADMIN"

# registro público: solo MEDICO o PACIENTE
curl -X POST http://localhost:8080/api/auth/registro -H "Content-Type: application/json" \
  -d '{"nombre":"Juan Gomez","email":"juan@medconnect.com","contrasena":"secreto123","role":"PACIENTE"}'
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
- **Gotcha real que pasó en esta sesión**: si arrancás un backend en un
  puerto alternativo (ej. `:8090`) y ya había uno viejo colgado ahí de una
  verificación anterior, `mvnw spring-boot:run` **falla en silencio** (el
  proceso nuevo no levanta, "Port already in use", pero el viejo sigue
  respondiendo) — un chequeo de salud tipo "pego a `/api/auth/login` y me
  da 401" **no lo distingue**, porque el proceso viejo también responde eso.
  Terminé verificando contra código desactualizado un buen rato sin darme
  cuenta. Antes de confiar en que un backend nuevo está arriba, confirmar en
  el log real la línea `Started MedConnectApplication` (no solo un 401
  cualquiera), o mejor: matar cualquier proceso viejo en ese puerto primero.

## Historial de PRs (todos mergeados salvo que se aclare lo contrario)

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
17. `fix/registro-publico-sin-admin` (PR #18) — el registro público aceptaba
    `role=ADMINISTRADOR` sin ninguna validación, así que cualquiera podía
    autoasignarse admin y saltear por completo la restricción del PR #17;
    ahora el registro público solo acepta `MEDICO`/`PACIENTE`.
18. `feature/historia-clinica` (PR #19) — arrancó como
    historia clínica compartida por paciente (`RegistroClinico`: crear/
    listar/exportar con permisos distintos para MEDICO/ADMINISTRADOR/
    PACIENTE, integrado en la fila de Turnos y de Pacientes), pero mientras
    el usuario lo probaba en vivo pidió una ronda grande de ajustes sobre
    la misma rama: `POST /api/turnos` pasó a ser solo ADMINISTRADOR;
    `MEDICO`/`PACIENTE` perdieron acceso a los directorios de
    médicos/pacientes; se agregó vinculación de identidad por email
    (`buscarPorEmail` en Medico/PacienteRepository) para que `MEDICO` vea
    solo sus pacientes/turnos y `PACIENTE` solo los suyos; `PACIENTE` puede
    cancelar (no confirmar) su propio turno, con un `ConfirmModal` doble;
    se agregó `Turno.preparacion`; `TurnoResponse`/`RegistroClinicoResponse`
    embeben nombres resueltos; y la sección "Crear turno" se rediseñó como
    "Otorgar turno" (buscar paciente por DNI + especialidad → médico
    filtrado). Ver los bloques dedicados de "Restricción por rol",
    "Historia clínica" y los dos bullets nuevos justo debajo, más arriba.

## Plan sugerido para la próxima sesión (sin decidir aún — preguntale al usuario)

Orden sugerido, de más a menos urgente — pero es una sugerencia, confirmar
con el usuario antes de arrancar cualquiera:

1. ~~Flujo para crear administradores.~~ **Hecho** (rama
   `feature/crear-administradores`): `POST /api/usuarios` (solo
   `ADMINISTRADOR`) permite dar de alta cualquier rol, incluido otro
   `ADMINISTRADOR`. Reutiliza `RegistrarUsuarioService` — se separó la
   validación común (`guardar()`, privado) de la restricción "no autoregistro
   como ADMINISTRADOR", que ahora solo aplica a `registrar()` (usado por
   `/api/auth/registro`, público); `registrarComoAdmin()` la salta. Nueva
   sección "Usuarios" en el frontend (`esAdmin`-only, arriba de Médicos) con
   un formulario simple nombre/email/contraseña/rol. Probado con Playwright
   contra Postgres real: login admin → crear ADMINISTRADOR nuevo → logout →
   login con la cuenta nueva → ve el panel de admin. Verificado también con
   curl que un token `MEDICO` o una request sin token dan 403.
2. ~~Mensaje claro cuando un médico/paciente no está vinculado.~~ **Hecho**
   (rama `feature/mensaje-usuario-no-vinculado`): nuevo `GET /api/medicos/me`
   (solo `MEDICO`) y `GET /api/pacientes/me` (solo `PACIENTE`) — devuelven el
   propio médico/paciente vinculado (200) o 404 si el email de la cuenta no
   coincide con ninguno. El frontend los llama una vez al cargar
   (`chequearVinculacion()`) y, si da 404, muestra un banner persistente
   arriba de todo (`vinculado === false`, solo para `esMedico`/`esPaciente`)
   explicando por qué las listas están vacías y pidiendo al admin que cargue
   el email exacto de la cuenta (`auth.email`, mostrado en el mensaje) en la
   ficha correspondiente. Verificado con Playwright: un `MEDICO` recién
   creado sin ficha vinculada ve el banner; el admin no lo ve nunca (no
   aplica a su rol).
3. ~~UI para vincular una cuenta de login con su Médico/Paciente.~~ **Hecho**
   (rama `feature/vincular-cuenta-perfil`): nuevo `GET /api/usuarios` (solo
   `ADMINISTRADOR`, sin `contrasena` en la respuesta — `UsuarioResponse` es
   un DTO propio) lista las cuentas de login existentes. En los forms de
   Médico/Paciente, el campo `Email` de texto libre se reemplazó por un
   `<select>` "Cuenta de acceso vinculada" que ofrece solo las cuentas del
   rol correspondiente (`MEDICO`/`PACIENTE`) que **todavía no están
   vinculadas a otro** médico/paciente (`emailsMedicosOcupados`/
   `emailsPacientesOcupados` en `App`, excluyendo el registro que se está
   editando). Si el email guardado no matchea ninguna cuenta (dato legado o
   cuenta borrada), se agrega una opción extra "(cuenta no encontrada)" para
   no perder el valor silenciosamente. Verificado con Playwright: crear una
   cuenta `MEDICO` nueva desde "Usuarios" → aparece en el selector del form
   de Médicos → al vincularla y guardar, deja de ofrecerse para otro médico.
4. Cerrar los huecos de ownership que quedaron pendientes: `GET
   /api/turnos/{id}` y `GET /api/pacientes/{id}` no filtran por dueño
   todavía (no explotado por la UI actual, pero un llamado directo a la API
   sí podría pedir un id ajeno).
5. Paginación en los listados, si el volumen de datos crece lo suficiente
   como para justificarlo — hoy no urge.
6. Eliminar un turno del todo, si alguna vez hace falta (hoy solo se puede
   cancelar, que es lo correcto para no perder historial — probablemente no
   haga falta un DELETE real).
