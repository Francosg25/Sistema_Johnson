# Etapa de construcción
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copiamos todo el contenido de la raíz al contenedor
COPY . .

# Entramos a la carpeta donde está el pom.xml
WORKDIR /app/johnson-sistema

# Compilamos
RUN mvn -B -DskipTests package

# Etapa de ejecución
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el .jar generado (ajustando la ruta de origen)
COPY --from=build /app/johnson-sistema/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]