<div align="center">
  
# MediConnect 
<br><br><img width="656" height="444" alt="Gemini_Generated_Image_eu3no2eu3no2eu3n" src="https://github.com/user-attachments/assets/28380c29-4c0a-4b1c-917c-b0f8ce626c3a" />


**Plataforma web Full Stack para gestión de clínicas médicas con reservas, agendas, historiales y facturación automatizada.**

</div>
<br>
<br>

## Objetivos
- Backend Java + Spring Boot con arquitectura limpia y roles claros (Administrador, Médico, Paciente).
- Frontend React + TypeScript con UI moderna, calendarios interactivos y formularios validados.
- Base de datos PostgreSQL y generación de facturas/PDF.

## Estructura propuesta
- `backend/` - dominio, casos de uso, infraestructura y adaptadores REST.
- `frontend/` - aplicación React con TypeScript y Tailwind.

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
1. Ir a `backend/`
2. Ejecutar `mvn clean install`
3. Ejecutar `mvn spring-boot:run`

### Frontend
1. Ir a `frontend/`
2. Ejecutar `npm install`
3. Ejecutar `npm run dev`
