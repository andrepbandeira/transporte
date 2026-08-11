# Etapa 1: Build — imagem Maven + Java 21 para compilar o JAR.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa 2: Run — JRE leve (Eclipse Temurin 21 JRE, Alpine).
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario nao-root para seguranca.
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/logs && chown -R app:app /app

USER app

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
