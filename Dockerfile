# ==========================================
# ETAPA 1: CONSTRUCCIÓN (BUILD)
# ==========================================
# Usando tu imagen original solicitada
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app/johnson-sistema

# OPTIMIZACIÓN DE CACHÉ ESTRICTA
# ... líneas anteriores ...

# OPTIMIZACIÓN DE CACHÉ ESTRICTA
COPY johnson-sistema/pom.xml .
RUN mvn -B dependency:go-offline -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true

COPY johnson-sistema/src ./src
RUN mvn -B -DskipTests package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true

# ... resto del archivo ...

# ==========================================
# ETAPA 2: EJECUCIÓN (RUNTIME)
# ==========================================
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update && \
    apt-get install -y postgresql-client && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/backups /app/evidencias && \
    chmod 777 /app/backups /app/evidencias

COPY --from=build /app/johnson-sistema/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]