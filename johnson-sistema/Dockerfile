FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copia todo el proyecto y compila
COPY . .
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia el JAR compilado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
