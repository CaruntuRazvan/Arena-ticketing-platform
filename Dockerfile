# 1. Imaginea de bază (Java 17)
FROM eclipse-temurin:17-jdk-alpine

# 2. Folderul unde va sta aplicația în interiorul Docker
WORKDIR /app

# 3. Copiem fișierul JAR generat de Maven în "cutie"
# (Presupunem că rulăm 'mvn clean package' înainte)
COPY target/*.jar app.jar

# 4. Spunem pe ce port rulează aplicația
EXPOSE 8081

# 5. Comanda de pornire
ENTRYPOINT ["java", "-jar", "app.jar"]