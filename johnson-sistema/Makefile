APP_DIR := johnson-sistema
COMPOSE := docker compose

.PHONY: help build docker-build up down restart logs logs-app shell db-shell seed test clean

help:
	@echo "Makefile targets:"
	@echo "  make build        -> compila el JAR usando Maven"
	@echo "  make docker-build -> construye imágenes con docker-compose"
	@echo "  make up           -> levanta servicios (db + app) en background"
	@echo "  make down         -> para y elimina contenedores y volúmenes"
	@echo "  make restart      -> reinicia app"
	@echo "  make logs         -> muestra logs de todos los servicios"
	@echo "  make logs-app     -> muestra logs del servicio app"
	@echo "  make shell        -> shell dentro del contenedor app"
	@echo "  make db-shell     -> psql dentro del contenedor de postgres"
	@echo "  make seed         -> intenta invocar /debug/crear-proyecto hasta que responda"
	@echo "  make test         -> ejecuta tests con Maven"
	@echo "  make clean        -> limpia build local y prune de docker"

build:
	@echo "Compilando JAR con Maven..."
	mvn -B -DskipTests package

docker-build:
	@echo "Construyendo imágenes con docker-compose..."
	$(COMPOSE) build --pull

up: docker-build
	@echo "Levantando servicios..."
	$(COMPOSE) up -d --remove-orphans

down:
	@echo "Parando servicios..."
	$(COMPOSE) down -v --remove-orphans

restart: down up

logs:
	$(COMPOSE) logs -f --tail=200

logs-app:
	$(COMPOSE) logs -f --tail=200 app

shell:
	@echo "Abriendo shell en contenedor 'app' (si existe)"
	$(COMPOSE) exec app /bin/sh

db-shell:
	@echo "Abriendo psql en contenedor 'db' (si existe)"
	$(COMPOSE) exec db psql -U johnson -d johnsondb

seed:
	@echo "Invocando /debug/crear-proyecto hasta que responda..."
	@count=0; \
	while [ $$count -lt 30 ]; do \
	  if curl -sSfS http://localhost:8081/debug/crear-proyecto > /dev/null; then \
	    echo "Seed exitosa"; exit 0; \
	  fi; \
	  count=$$((count+1)); sleep 2; \
	done; \
	echo "No se pudo ejecutar endpoint de seed en 60s"; exit 1

test:
	cd $(APP_DIR) && mvn test

clean:
	cd $(APP_DIR) && mvn clean
	docker system prune -af || true
