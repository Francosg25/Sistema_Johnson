# Johnson Electric - APQP System Makefile
# Franco Sanchez

.PHONY: db-dev dev down-dev up-prod down-prod build logs clean help

# Ayuda: Muestra los comandos disponibles
help:
	@echo "Johnson Electric - Sistema de Gestion APQP"
	@echo "------------------------------------------"
	@echo "[ ENTORNO DE DESARROLLO - WINDOWS / IDE ]"
	@echo "make db-dev   - Inicia SOLO la base de datos de desarrollo (Puerto 5444 ABIERTO)"
	@echo "make dev      - Inicia DB Dev y arranca Spring Boot localmente"
	@echo "make down-dev - Detiene la base de datos de desarrollo"
	@echo ""
	@echo "[ ENTORNO DE PRODUCCION / PRUEBAS - DOCKER ]"
	@echo "make up-prod  - Levanta TODO el stack seguro (Nginx HTTPS + App + DB Blindada)"
	@echo "make down-prod- Detiene el stack completo de produccion"
	@echo "make logs     - Muestra los logs del contenedor Java en produccion"
	@echo ""
	@echo "[ UTILIDADES ]"
	@echo "make build    - Compila el proyecto Java omitiendo pruebas"
	@echo "make clean    - Limpieza profunda (Maven + Elimina Volumenes DEV y PROD)"

# ==========================================
# ENTORNO DE DESARROLLO (LOCAL)
# ==========================================
db-dev:
	docker compose -f docker-compose-dev.yml up -d db

dev: db-dev
	@echo "--- Base de datos Dev iniciada. Esperando 20 segundos de estabilizacion... ---"
	@timeout /t 20 /nobreak > NUL
	@echo "--- Iniciando aplicacion Spring Boot local... ---"
	cd johnson-sistema && mvn spring-boot:run

down-dev:
	docker compose -f docker-compose-dev.yml down

# ==========================================
# ENTORNO DE PRODUCCION (DOCKER STACK)
# ==========================================
up-prod:
	docker compose up --build -d

down-prod:
	docker compose down

logs:
	docker compose logs -f app

# ==========================================
# COMPILACION Y LIMPIEZA
# ==========================================
build:
	cd johnson-sistema && mvn clean install -DskipTests

clean:
	@echo "--- Deteniendo todos los contenedores posibles ---"
	docker compose down
	docker compose -f docker-compose-dev.yml down
	@echo "--- Eliminando volumenes de base de datos ---"
	-rd /s /q postgres_data
	-rd /s /q postgres_data_dev
	@echo "--- Limpiando dependencias de Maven ---"
	cd johnson-sistema && mvn clean  
	@echo "--- Purgando Docker ---"
	docker system prune -f