# Etapa 1: Construcción del JAR
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia solo el POM primero para aprovechar la caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Luego copia el resto del código fuente
COPY src ./src

# Compila el proyecto sin ejecutar tests
RUN mvn clean package -DskipTests

# ------------------------------------------------------

# Etapa 2: Imagen final minimalista
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copiamos solo el JAR resultante
COPY --from=builder /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Configuración mínima de JVM para entornos productivos
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Usa exec form para evitar problemas de señal con Docker
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
