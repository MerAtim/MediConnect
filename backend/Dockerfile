FROM eclipse-temurin:25-jre as runtime
WORKDIR /app

# Copy pre-built jar from host (run `mvn -DskipTests package` before building the image)
COPY target/medconnect-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
