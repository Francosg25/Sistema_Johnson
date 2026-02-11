# Etapa de construcción
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# COPIAMOS TODO EL CONTEXTO
COPY . .

# CAMBIAMOS AL DIRECTORIO DONDE ESTÁ EL POM.XML
# Según tus archivos, la carpeta es 'johnson-sistema'
WORKDIR /app/johnson-sistema

# Ejecutamos la compilación desde esa carpeta
RUN mvn -B -DskipTests package

# Etapa de ejecución
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el archivo .jar generado desde la carpeta target de la subcarpeta
COPY --from=build /app/johnson-sistema/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]