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
### Backend
Necesita PostgreSQL corriendo (por defecto en `localhost:5432`, DB `medconnect`,
user `postgres`) — ver `spring.datasource.*` en
`backend/src/main/resources/application.properties` para los defaults y las
env vars que los overridean.
1. Ir a `backend/`
2. Ejecutar `DB_PASSWORD=<tu-password> ./mvnw spring-boot:run` (Windows:
   `./mvnw.cmd spring-boot:run`, o `$env:DB_PASSWORD='...'` en PowerShell)

### Frontend
1. Ir a `frontend/`
2. Ejecutar `npm install`
3. (Opcional) copiar `.env.example` a `.env` y ajustar `VITE_API_URL` si el
   backend no corre en `http://localhost:8080`
4. Ejecutar `npm run dev`
