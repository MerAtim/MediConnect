<div align="center">
  
# MediConnect 
<br><br><img width="656" height="444" alt="Gemini_Generated_Image_eu3no2eu3no2eu3n" src="https://github.com/user-attachments/assets/28380c29-4c0a-4b1c-917c-b0f8ce626c3a" />


**Plataforma web para gestión de turnos e historias clínicas en clínicas médicas, con roles de Administrador, Médico y Paciente.**

</div>
<br>
<br>

## Qué hace hoy
- Turnos: alta (solo Administrador), listado paginado y filtrable, cambio de estado (confirmar/cancelar) con reglas por rol.
- Médicos y Pacientes: alta/edición/baja (Administrador), directorios acotados por rol — un Médico solo ve a sus propios pacientes.
- Historia clínica: registros por paciente (Médico) y exportación a texto descargable (Administrador).
- Cuentas de acceso separadas de las fichas de Médico/Paciente, con vinculación por email (un email = un médico/paciente, forzado a nivel de base).
- Autenticación con JWT y autorización por rol en cada endpoint.

No es un proyecto terminado: es una app que se sigue construyendo de forma incremental (ver `CONTINUE_HERE.md` para el detalle de qué se implementó en cada etapa y qué queda pendiente).

## Stack
- **Backend**: Java 17 + Spring Boot 3.2, arquitectura hexagonal (dominio / casos de uso / infraestructura / adaptadores REST), PostgreSQL vía JPA.
- **Frontend**: React 18 + Vite, JavaScript (no TypeScript), Tailwind CSS.
- **Tests**: JUnit + Mockito en el backend, Vitest + React Testing Library en el frontend. CI en GitHub Actions corre ambos en cada push/PR a `main`/`develop`.

## Estructura del proyecto
- `backend/` - dominio, casos de uso, infraestructura y adaptadores REST.
- `frontend/` - aplicación React (Vite + Tailwind).
- `CONTINUE_HERE.md` - bitácora técnica detallada del proyecto: qué se implementó, decisiones tomadas y por qué, y qué queda pendiente.

## Buenas prácticas aplicadas
- Arquitectura limpia (Clean Architecture / Hexagonal).
- Principios SOLID y separación de responsabilidades.
- Commits atómicos y convencionales en español.
- Branching strategy basada en Git Flow simplificado.

## Branching strategy
- `main` - código de producción estable.
- `develop` - integración de funcionalidades completas.
- `feature/<nombre>` - desarrollo de funcionalidades nuevas.
- `hotfix/<tema>` - correcciones urgentes desde `main`.

## Convenciones de commit
Ejemplos en español:
- `feat: agregar modelo de turno y repositorio de citas`
- `fix: corregir validación de fecha de turno`
- `docs: actualizar README con estrategia de ramas`
- `chore: agregar gitignore y README inicial`
- `refactor: separar casos de uso de dominio`

## Instalación y ejecución
### Con Docker Compose (recomendado — levanta todo con un comando)
1. Copiar `.env.example` a `.env` y completar `DB_PASSWORD`, `JWT_SECRET` y
   `ENCRYPTION_KEY` (generar los dos últimos con `openssl rand -base64 48`
   y `openssl rand -base64 32` respectivamente — no reutilizar ningún
   valor de ejemplo).
2. `docker compose up --build`
3. Backend en `http://localhost:8080` (o el puerto que hayas puesto en
   `BACKEND_PORT`), frontend en `http://localhost:80`. Postgres corre
   dentro de la red de compose, con su propio volumen (`postgres_data`) —
   no hace falta tener Postgres instalado en el host.

Cada servicio expone healthcheck propio (`docker compose ps` muestra
`healthy`/`unhealthy`); el backend no arranca hasta que Postgres esté
realmente aceptando conexiones, y el frontend no arranca hasta que el
backend responda `UP` en `/actuator/health`.

### Sin Docker (desarrollo día a día)
#### Backend
Necesita PostgreSQL corriendo (por defecto en `localhost:5432`, DB `medconnect`,
user `postgres`) — ver `spring.datasource.*` en
`backend/src/main/resources/application.properties` para los defaults y las
env vars que los overridean.
1. Ir a `backend/`
2. Ejecutar `DB_PASSWORD=<tu-password> ./mvnw spring-boot:run` (Windows:
   `./mvnw.cmd spring-boot:run`, o `$env:DB_PASSWORD='...'` en PowerShell)
3. Documentación interactiva de la API (Swagger UI) en
   `http://localhost:8080/swagger-ui.html` — para probar un endpoint
   protegido con "Try it out" hay que estar logueado en la app desde el
   mismo navegador (el JWT viaja en una cookie httpOnly, no hay forma de
   pegarlo a mano en Swagger).

#### Frontend
1. Ir a `frontend/`
2. Ejecutar `npm install`
3. (Opcional) copiar `.env.example` a `.env` y ajustar `VITE_API_URL` si el
   backend no corre en `http://localhost:8080`
4. Ejecutar `npm run dev`
