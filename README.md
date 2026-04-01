
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
Antes de tocar cualquier código, el equipo de TI debe provisionar el siguiente entorno en el clúster local (ej. VMware/Hyper-V):

Máquina Virtual (VM):

OS: Ubuntu Server 22.04 LTS o superior.

Recursos: 2 a 4 vCPUs, 8 GB de RAM, 50 GB de almacenamiento SSD.

Red: Dirección IP Estática en la red de servidores (VLAN correspondiente).

Configuración de Red (Firewall y Ruteo):

Puertos de Entrada: * TCP/22 (SSH) - Acceso restringido solo a las IPs del equipo de desarrollo/administradores.

TCP/80 (HTTP) y TCP/443 (HTTPS) - Abiertos para el tráfico de la intranet corporativa.

Ruteo Inter-Site: Asegurar que las subredes de las plantas de México y EE. UU. tengan alcance (routing) hacia la IP estática de este servidor a través de la VPN Site-to-Site corporativa.

DNS Interno (Active Directory / Windows Server DNS):

Crear un Registro A que apunte la IP estática del servidor a un dominio interno amigable.

Ejemplo: apqp-system.johnsonelectric.local -> 10.X.X.X

Fase 2: Preparación del Servidor (Para el Administrador Linux / DevOps)
Una vez entregada la máquina con acceso SSH, se deben instalar las dependencias base.

1. Actualizar el sistema:
Bash
sudo apt update && sudo apt upgrade -y
2. Instalar Docker y Docker Compose:
Bash
# Descargar el script oficial de instalación de Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Agregar el usuario actual al grupo de docker para no usar 'sudo' en cada comando
sudo usermod -aG docker $USER
newgrp docker

3. Instalar Git:
Bash
sudo apt install git -y
Fase 3: Despliegue de la Aplicación
Con el servidor listo, se procede a transferir y ejecutar el código del sistema.



1. Clonar el repositorio del proyecto:
Navegar al directorio de aplicaciones (usualmente /opt) y descargar el código fuente.

Bash
cd /opt
sudo git clone <URL_DE_REPOSITORIO> sistema-apqp
sudo chown -R $USER:$USER /opt/sistema-apqp
cd sistema-apqp


2. Configurar Variables de Entorno:
Sefinir las contraseñas reales de producción. Crear un archivo .env en la raíz del proyecto para que Docker Compose lo consuma.

Bash
nano .env
Contenido del .env:

Fragmento de código
DB_NAME=
DB_USER=
DB_PASSWORD=


3. Levantar los Contenedores:
Ejecutar Docker Compose en modo "detached" (en segundo plano).

Bash
docker compose up -d --build
Docker descargará las imágenes, compilará el .jar de Java, levantará PostgreSQL y configurará Nginx automáticamente.

4. Verificar el estado de los servicios:

Bash
docker ps
(Debe mostrar los contenedores de base de datos, backend y proxy corriendo con el status "Up").

Fase 4: Validaciones Post-Despliegue
Prueba Local: Dentro del servidor, ejecutar curl http://localhost para confirmar que Nginx responde con el HTML del sistema.

Prueba de Red: Desde una computadora de un ingeniero en la planta de México, abrir el navegador e ingresar al dominio interno: http://apqp-system.johnsonelectric.local.

Prueba Inter-Site: Repetir el paso anterior desde una computadora conectada a la red de la planta de Estados Unidos. Ambos usuarios deben ver la pantalla de Login y tu lógica de roles en Spring Boot aislará los datos automáticamente al iniciar sesión.



