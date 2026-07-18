# ---------- Стадия 1: сборка jar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# ---------- Стадия 2: лёгкий образ для запуска ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/placardo-1.0.0.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
