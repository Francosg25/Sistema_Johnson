
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


----------Notas para el Departamento de IT
Puerto de Entrada: 80 (HTTP).

Puerto Interno App: 8081 (No requiere apertura en firewall externo).

DNS Sugerido: Se recomienda apuntar sistema-apqp.local a la IP estática del servidor asignado.

Memoria: El contenedor de Java está optimizado para ejecutarse en entornos con recursos controlados (JRE 17 sobre Alpine/Debian).



