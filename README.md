
-------Sistema de Gestión APQP - Johnson Electric


La aplicación utiliza una arquitectura moderna basada en microservicios contenerizados para garantizar estabilidad, seguridad y facilidad de despliegue.

-------Tecnologías Principales
Backend: Java 17 / Spring Boot 3.5.10

Base de Datos: PostgreSQL 15

Migraciones: Flyway (Versionado automático de esquemas)

Proxy Inverso: Nginx (Servidor de entrada y seguridad)

Contenerización: Docker & Docker Compose



--------Comandos de Operación (Makefile)
El proyecto incluye un Makefile para simplificar las tareas de administración. Ejecutar estos comandos desde la terminal en la carpeta raíz:


ESTO SOLO SI NO TIENES MAKE INSTALADO

1. Abra PowerShell como administrador. 
2. Instale Chocolatey ejecutando:
Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

3. Una vez instalado, ejecute el siguiente comando para instalar Make:
choco install make

Comando	Acción
make up	Despliegue Total: Compila, construye y levanta todo el sistema en segundo plano.
make down	Apagado: Detiene y remueve los contenedores de forma segura.
make logs	Monitoreo: Muestra los logs en tiempo real de la aplicación Java.
make dev	Modo Desarrollo: Levanta la DB en Docker pero permite correr el código localmente.
make clean	Mantenimiento: Borra imágenes, volúmenes y archivos temporales de compilación.
Nota: Si el servidor no tiene make, usar: docker compose up --build -d.




--------Estructura y Persistencia
Para evitar la pérdida de información ante fallas eléctricas o reinicios, el sistema utiliza volúmenes persistentes en el servidor:

/postgres_data: Almacena físicamente la base de datos PostgreSQL. No borrar.

/evidencias: Carpeta donde se guardan los documentos y archivos subidos por los usuarios.

/backups: Carpeta de destino para los respaldos automáticos del sistema.



----------Seguridad e Infraestructura
Nginx como Escudo: La aplicación Java no está expuesta directamente a la red. Todo el tráfico entra por el puerto 80, donde Nginx actúa como filtro y Reverse Proxy.

Rate Limiting: El sistema protege la API contra ataques de fuerza bruta o saturación, limitando a 20 peticiones por minuto por IP en rutas críticas.

Flyway: La base de datos se versiona sola. Al iniciar, el sistema revisa la tabla flyway_schema_history para asegurar que el esquema esté actualizado.

Políticas de Reinicio: Todos los servicios tienen restart: always. Si el servidor se apaga, el sistema APQP se iniciará automáticamente al recuperar la energía.


----------Notas para IT
Puerto de Entrada: 80 (HTTP).

Puerto Interno App: 8081 (No requiere apertura en firewall externo).

DNS Sugerido: Se recomienda apuntar sistema-apqp.local a la IP estática del servidor asignado.

Memoria: El contenedor de Java está optimizado para ejecutarse en entornos con recursos controlados (JRE 17 sobre Alpine/Debian).

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------
Se requiere el aprovisionamiento de una Máquina Virtual (VM) dentro del clúster de la planta 

Red y Accesos: * Asignar una Dirección IP Estática interna.

Crear un registro DNS en la intranet (Ejemplo: apqp-system.johnsonelectric.local apuntando a la IP estática).

Reglas de Firewall (Puertos):

TCP/22 (SSH) - Abierto solo para el equipo de desarrollo/administradores.

TCP/80 (HTTP) y TCP/443 (HTTPS) - Abiertos para tráfico de la Intranet corporativa.

FASE 2: Preparación del Entorno (Administrador Linux / DevOps)
Una vez entregada la VM, acceder por SSH e instalar las herramientas base.

1. Actualización del sistema operativo:

Bash
sudo apt update && sudo apt upgrade -y
2. Instalación del motor de Docker y Docker Compose:

Bash
# Descarga e instalación automatizada de Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Agregar al usuario actual al grupo Docker para evitar uso excesivo de 'sudo'
sudo usermod -aG docker $USER
newgrp docker
3. Instalación de Git:

Bash
sudo apt install git -y
FASE 3: Despliegue de la Aplicación
El código fuente está centralizado y listo para su descarga y ejecución.

1. Obtener el código fuente:
Navegar al directorio de instalación (recomendado /opt) y clonar el repositorio.

Bash
cd /opt
sudo git clone https://github.com/Francosg25/Sistema_Johnson.git sistema-apqp
sudo chown -R $USER:$USER /opt/sistema-apqp
cd sistema-apqp
2. Configuración de Variables de Entorno (Credenciales de Producción):
Crear el archivo de configuración .env en la raíz del proyecto. Este archivo no está en el repositorio por seguridad y debe ser creado manualmente en el servidor.

Bash
nano .env
Pegar la siguiente plantilla y llenar con los datos definitivos de producción:

Fragmento de código
# Configuración de Base de Datos
DB_NAME=johnsondb
DB_USER=admin
DB_PASSWORD=johnsonbase2026

# Configuración de Servidor de Correos (Alertas y Notificaciones)
MAIL_USERNAME=johnsonelectricapqp@gmail.com
MAIL_PASSWORD=jviahxfgodjajgnp

# Credenciales de Administrador del Sistema
ADMIN_DEFAULT_PASSWORD=adminpass

# URL Base para enlaces en los correos (Usar el dominio DNS asignado por TI)
APP_BASE_URL=http://apqp-system.johnsonelectric.local
(Guardar y salir del editor nano con Ctrl+O, Enter, Ctrl+X).

3. Inicialización de los Servicios:
Ejecutar Docker Compose para descargar las imágenes, compilar el código Java y levantar los servicios en segundo plano.

Bash
docker compose up -d --build

4. Verificación de Estado:
Comprobar que los tres contenedores (PostgreSQL, Spring Boot y Nginx) estén en estado "Up".

Bash
docker ps
FASE 4: Mantenimiento y Respaldo (Crítico)
Persistencia de Datos:
La base de datos PostgreSQL utiliza un "Volumen Nombrado" en Docker para proteger la información contra reinicios o caídas.

ADVERTENCIA: Nunca ejecutar docker compose down -v en este servidor, ya que la bandera -v eliminará los volúmenes físicos de la base de datos de Johnson Electric. Para reiniciar el sistema de forma segura, utilizar únicamente: docker compose restart.