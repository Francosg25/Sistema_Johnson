# Etapa de construcción
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copiamos todo el contenido de la raíz al contenedor 
COPY . .
# Entramos a la carpeta donde está el pom.xml 
WORKDIR /app/johnson-sistema

# Compilamos el proyecto (omitiendo tests para velocidad)
RUN mvn -B -DskipTests package

# Etapa de ejecución
FROM eclipse-temurin:17-jre
WORKDIR /app

# INSTALACIÓN CRÍTICA DE CLIENTE POSTGRESQL (Necesario para que el comando pg_dump de Java funcione)
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

# Creamos directorios para backups y evidencias con permisos de escritura total
RUN mkdir -p /app/backups /app/evidencias && chmod 777 /app/backups /app/evidencias

# Copiamos el .jar generado de la etapa anterior
COPY --from=build /app/johnson-sistema/target/*.jar app.jar

EXPOSE 8081

# Ejecución optimizada para contenedores
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]