# Reanudar trabajo — MedConnect

Este archivo se mantiene actualizado al final de cada sesión de trabajo para que,
aunque pasen días sin conectarte, se pueda seguir sin releer el proyecto entero.
Última actualización: **2026-08-16**, tras mergear PR #10 (ciclo de vida del turno).

## Stack y arquitectura

- **Backend**: Spring Boot 3.2 / Java 17, Maven (`backend/`).
- **Persistencia**: JPA + **PostgreSQL real** (no H2, no Docker). Requiere Postgres
  instalado nativamente en Windows, DB `medconnect`, user `postgres`.
  `spring.jpa.hibernate.ddl-auto=update` crea/actualiza las tablas solo.
- **Seguridad**: `SecurityConfig` con `/api/**` abierto (`permitAll`), CSRF
  deshabilitado. No hay autenticación real todavía (no hay login, `Usuario`/
  `UsuarioRole` existen como modelo de dominio pero sin persistencia ni uso).
- **Frontend**: React 18 + Vite 5 (`frontend/`), `fetch` plano (sin axios).
- **Estilos**: Tailwind CSS v3, paleta teal/slate ("clínico pero no frío"),
  contrato de clases reutilizables en `frontend/src/index.css`
  (`.card`, `.heading`, `.label`, `.input-field`, `.btn-primary`, `.btn-secondary`,
  `.badge*`). **Usar siempre estas clases** para mantener coherencia visual, no
  clases Tailwind sueltas para botones/inputs/cards.
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

  **Si agregás una entidad nueva, copiá este patrón exacto** — no inventar uno nuevo.

## Qué está implementado (PRs #1–#10, todos mergeados a `develop`)

- **Turno**: crear (`POST /api/turnos`, valida solapamiento por médico+fecha,
  y que `medicoId`/`pacienteId` existan realmente), buscar por id
  (`GET /api/turnos/{id}` → 404 si no existe), listar con filtro opcional por
  `medicoId` o `pacienteId` (`GET /api/turnos?medicoId=..&pacienteId=..`),
  cambiar estado (`PATCH /api/turnos/{id}/estado`, body `{"estado":"CONFIRMADO"}`
  o `"CANCELADO"`; un turno `CANCELADO` no puede volver a modificarse → 400;
  estado inválido en el body → 400; id inexistente → 404).
- **Médico**: crear (`POST /api/medicos`, valida nombre/especialidad/matrícula
  obligatorios), listar (`GET /api/medicos`), buscar por id (`GET /api/medicos/{id}`).
- **Paciente**: crear (`POST /api/pacientes`, valida nombre/dni obligatorios),
  listar (`GET /api/pacientes`), buscar por id (`GET /api/pacientes/{id}`).
- **Frontend** (`frontend/src/App.jsx`, un solo archivo):
  - Sección "Médicos": form de alta + tabla.
  - Sección "Pacientes": form de alta + tabla.
  - Sección "Crear turno": selects de médico/paciente (poblados desde las APIs
    de arriba, ya no son inputs numéricos de ID).
  - Sección "Turnos": tabla con auto-carga al abrir, refresco automático tras
    crear un turno, filtro por médico ID / paciente ID, botón "Ver todos".
    Resuelve nombre de médico/paciente por id (fallback a `#id` si no está en
    la lista cargada, p. ej. turnos de prueba con IDs que ya no existen).
    Columna "Acciones" con botón Confirmar (solo si `PENDIENTE`) y Cancelar
    (si no está ya `CANCELADO`), llaman al `PATCH .../estado` y refrescan.
- Tests: 32 tests (unitarios de casos de uso + MockMvc de controllers +
  integración end-to-end con repos fake en memoria vía `@Profile("test")`).
  Todos verificados también contra Postgres real con curl y en navegador real
  con Playwright (no solo tests automatizados).

## Qué NO está implementado todavía (huecos conocidos)

- Editar / eliminar médico o paciente (solo alta y lectura).
- Autenticación/login real (no hay endpoint de `Usuario`, ni JWT, ni sesiones;
  `/api/**` está totalmente abierto a propósito por ahora).
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

```bash
./mvnw.cmd test   # 32 tests, no necesita Postgres levantado (usa fakes en memoria)
```

### Frontend
```bash
cd frontend
npm install   # solo si no están instaladas las deps
npm run dev
```
**Ojo**: el puerto `5173` puede estar ocupado por otro proyecto del usuario
("Campus Academia Mariana Casella", corre en Windows en ese puerto). Vite cae
solo al siguiente puerto libre (normalmente `5174`) — mirá el log de `npm run dev`
para confirmar el puerto real antes de probar en el navegador.

### Probar endpoints (ejemplos)
```bash
curl -X POST http://localhost:8080/api/medicos -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Pérez","especialidad":"Cardiología","matricula":"MP1234"}'

curl -X POST http://localhost:8080/api/pacientes -H "Content-Type: application/json" \
  -d '{"nombre":"Juan Gómez","dni":"30111222"}'

curl -X POST http://localhost:8080/api/turnos -H "Content-Type: application/json" \
  -d '{"fechaHora":"2026-08-12T10:00:00","especialidad":"Cardiología","medicoId":1,"pacienteId":1}'

curl http://localhost:8080/api/turnos
curl http://localhost:8080/api/medicos
curl http://localhost:8080/api/pacientes
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

## Siguientes pasos sugeridos (sin decidir aún — preguntale al usuario)

- Editar/eliminar médico y paciente.
- Autenticación básica (login de `Usuario`, roles `ADMINISTRADOR`/`MEDICO`/`PACIENTE`).
- Paginación en los listados si el volumen de datos crece.
