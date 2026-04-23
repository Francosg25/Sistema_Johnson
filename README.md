
-------Sistema de Gestión APQP - Johnson Electric


La aplicación utiliza una arquitectura moderna basada en microservicios contenerizados para garantizar estabilidad, seguridad y facilidad de despliegue.

-------Tecnologías Principales
Backend: Java 17 / Spring Boot 3.5.10

Base de Datos: PostgreSQL 15

Migraciones: Flyway (Versionado automático de esquemas)

Proxy Inverso: Nginx (Servidor de entrada y seguridad)

Contenerización: Docker & Docker Compose


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
Red y Accesos: * Asignar una Dirección IP Estática interna.

Crear un registro DNS en la intranet (Ejemplo: apqp-system.johnsonelectric.local apuntando a la IP estática).

Reglas de Firewall (Puertos):

TCP/22 (SSH) - Abierto solo para el equipo de desarrollo/administradores.

TCP/80 (HTTP) y TCP/443 (HTTPS) - Abiertos para tráfico de la Intranet corporativa.


ADVERTENCIA: Nunca ejecutar docker compose down -v en este servidor, ya que la bandera -v eliminará los volúmenes físicos de la base de datos de Johnson Electric. Para reiniciar el sistema de forma segura, utilizar únicamente: docker compose restart.
