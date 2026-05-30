# Stage 1: Compilare Java
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiem pom.xml pentru a descărca dependențele în cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiem codul sursă și generăm fișierul .jar final
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Rularea aplicației într-un container curat și ușor
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# HEALTHCHECK
RUN apk add --no-cache curl

# Copiem JAR-ul compilat anterior
COPY --from=build /app/target/*.jar app.jar

# Utilizator securizat non-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Portul standard de Eureka pe care îl folosești
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]