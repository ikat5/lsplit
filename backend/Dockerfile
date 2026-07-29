# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Build the application
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the fat jar produced by the build stage
COPY --from=build /app/target/*.jar app.jar

# Render (and most PaaS) inject the port via $PORT; Spring reads it in application.yml
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
