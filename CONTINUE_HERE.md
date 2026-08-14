# Reanudar trabajo — MedConnect

Archivo de ayuda rápida para reanudar el trabajo cuando vuelvas.

## Resumen rápido (estado actual)
- Backend: Spring Boot (Java 25), endpoints de turno creados en `backend/src/main/java/com/medconnect/interfaces/rest/TurnoController.java`.
- Persistencia: JPA + H2 en `application.properties` para desarrollo. Repositorio en `backend/src/main/java/com/medconnect/infrastructure/persistence`.
- Frontend: React + Vite en `frontend/`. Añadido formulario demo en `frontend/src/App.jsx` que hace POST a `/api/turnos`.
- Tests: `mvnw` corre `clean test` y la suite de pruebas básicas/integración pasa en el entorno actual.

---

## Comandos útiles
Usá la terminal en la carpeta raíz del proyecto o movete a `backend` / `frontend` según convenga.

### Backend (Windows / PowerShell)
```powershell
cd backend
# Ejecutar tests
.\mvnw.cmd clean test
# Ejecutar aplicación (levanta servidor en :8080)
.\mvnw.cmd spring-boot:run
```

### Frontend
```bash
cd frontend
npm install   # solo si no se instalaron dependencias
npm run dev    # levanta Vite en http://localhost:5173
```

### Probar endpoints (ejemplos)
- Crear turno:
```bash
curl -X POST http://localhost:8080/api/turnos \
  -H "Content-Type: application/json" \
  -d '{"fechaHora":"2026-08-12T10:00:00","especialidad":"Cardiología","medicoId":2,"pacienteId":3}'
```
- Obtener turno por id:
```bash
curl http://localhost:8080/api/turnos/1
```

---

## Archivos clave
- `backend/pom.xml` — dependencias y plugins
- `backend/src/main/java/com/medconnect/MedConnectApplication.java` — arranque de Spring Boot
- `backend/src/main/java/com/medconnect/interfaces/rest/TurnoController.java` — endpoints REST
- `backend/src/main/java/com/medconnect/application/usecase/` — servicios (crear/buscar)
- `backend/src/main/java/com/medconnect/infrastructure/persistence/` — entidad y repositorios JPA
- `frontend/src/App.jsx` — formulario demo para crear turnos

---

## Siguientes pasos sugeridos (cuando vuelvas)
- Confirmar que `backend` y `frontend` arrancan localmente y probar el formulario.
- Añadir endpoint para listar todos los turnos (GET `/api/turnos/all` o similar).
- Mejorar validaciones y errores en `TurnoController`.
- Agregar más pruebas automatizadas e integración CI si corresponde.

---

## Prompt para reanudar (Copia y pega esto en la próxima sesión)
Rellena las partes entre `<>` y pega el bloque en la conversación con el asistente para que retome exactamente desde donde quedaste.

```
Reanudar sesión MedConnect
- Fecha: <2026-08-13>
- Branch actual: <nombre_branch_o_N/A>
- Estado local:
  - ¿Backend corriendo? (sí/no): <>
  - ¿Frontend corriendo? (sí/no): <>
  - Último comando ejecutado: <ej: .\mvnw.cmd spring-boot:run>
- Qué hicimos antes: CORS habilitado, controlador de turnos creado, formulario demo en frontend
- Objetivo ahora: <por ejemplo: "Integrar formulario con listado de turnos" o "Agregar endpoint listar todos los turnos">
- Preferencias: <tests antes/después, usar H2/local Postgres, crear PR en branch X>
- Archivos relevantes: backend/src/main/java/com/medconnect/interfaces/rest/TurnoController.java, frontend/src/App.jsx

Acciones pequeñas que podés pedirme inmediatamente:
- "Arranca backend y corre tests" — yo ejecutaré `mvnw clean test`
- "Proba crear turno desde UI" — yo te guío para abrir el navegador y verificar
- "Agrega endpoint listar todos los turnos" — implemento y corro tests
```

---
