# Johnson Electric - APQP System Makefile
# Franco Sanchez

.PHONY: db dev build up down logs clean help

# Ayuda: Muestra los comandos disponibles
help:
	@echo "Johnson Electric - Sistema de Gestión APQP"
	@echo "------------------------------------------"
	@echo "make db      - Inicia solo la base de datos (Postgres)"
	@echo "make dev     - Inicia la DB y corre la App localmente (Modo Desarrollo)"
	@echo "make build   - Compila el proyecto con Maven"
	@echo "make up      - Levanta TODO el sistema en Docker (App + DB)"
	@echo "make down    - Detiene todos los contenedores"
	@echo "make logs    - Muestra los logs de la aplicación"
	@echo "make clean   - Limpieza profunda (Maven + Docker Volumes)"

# Inicia solo la base de datos en Docker (Persistente)
db:
	docker compose up -d db

# Modo Desarrollo: Inicia la DB y corre Spring Boot en tu Windows (Más rápido para programar)
dev: db
	cd johnson-sistema && mvn spring-boot:run

# Compilación pura de Java
build:
	cd johnson-sistema && ./mvnw clean install -DskipTests

# Levanta todo el stack (Ideal para demostraciones finales)
up:
	docker compose up --build -d

# Detener el sistema
down:
	docker compose down

# Ver qué está pasando en la App
logs:
	docker compose logs -f app

# Limpieza total: Borra la DB (para empezar de cero) y archivos temporales de Java
clean:
	# Busca la sección clean:
clean:
	docker compose down -v
	cd johnson-sistema && mvnw.cmd clean  
	docker system prune -f