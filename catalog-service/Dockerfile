FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# HEALTHCHECK
RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs
USER spring:spring

# Portul standard pentru Config Server
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]