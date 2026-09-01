# Build multi-stage: no depende de un Maven instalado en el host ni de un
# jar pre-compilado a mano -- `docker build .` alcanza y solita.
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# El wrapper y el pom se copian antes que el codigo para que Docker cachee
# la descarga de dependencias en su propia capa: mientras pom.xml no
# cambie, un rebuild tras tocar solo src/ no vuelve a bajar todo Maven
# Central de nuevo.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
# Los tests ya corren en CI antes de llegar a esta imagen; repetirlos aca
# solo alarga el build sin agregar cobertura nueva.
RUN ./mvnw -B -DskipTests package

# Imagen runtime: solo el JRE (no el JDK completo) y alpine, coherente con
# java.version=17 del pom -- la imagen anterior corria un JRE 25 sin
# ninguna razon para estar dos major versions por delante del target real.
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# No correr como root dentro del contenedor: si algun dia se encuentra una
# RCE en una dependencia, un usuario sin privilegios limita el blast radius
# a lo que este usuario puede tocar, no a todo el filesystem de la imagen.
RUN addgroup -S medconnect && adduser -S medconnect -G medconnect
COPY --from=build --chown=medconnect:medconnect /app/target/medconnect-backend-0.0.1-SNAPSHOT.jar app.jar
USER medconnect

EXPOSE 8080

# Usa el endpoint agregado en el PR de Actuator: start-period le da tiempo
# a Flyway + Hibernate a terminar de arrancar antes de que el primer check
# cuente como fallo.
HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]
