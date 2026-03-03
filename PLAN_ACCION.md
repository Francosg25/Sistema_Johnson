# Plan de Acción Rápido para Mejorar Rendimiento

1. **Actualizar dependencias y configuración:**
   - Ejecutar `mvn clean install` para compilar.
   - Verificar que `spring-boot-starter-cache` y `caffeine` estén en `pom.xml`.

2. **Iniciar contenedores Docker:**
   ```bash
   docker-compose up -d
   ```
   Esperar a que el servicio `johnson_db` esté saludable.

3. **Crear índices en BD:**
   ```bash
   psql -h localhost -p 5444 -U admin -d johnsondb -f DATABASE_OPTIMIZATION.sql
   ```
   (usar contraseña `johnsonbase2026`)

4. **Iniciar aplicación:**
   ```bash
   cd johnson-sistema
   mvn spring-boot:run
   ```

5. **Verificar caches:**
   - Acceder a `/proyectos/checklist/1` varias veces; debería mostrar logs de cache hit.
   - Consultar `/proyectos/api/reportes/progreso` devuelve JSON rápido.

6. **Probar navegación:**
   - Página principal (`/`) con paginación activa.
   - Reportes se cargan asíncronamente (mirar devtools network).

7. **Monitoreo:**
   - Habilitar Actuator y ver métricas de cache (`/actuator/metrics`).
   - Revisar el uso de CPU/DB con `pg_stat_statements`.

8. **Mantenimiento continuo:**
   - Agregar índices adicionales si se detectan queries lentas.
   - Ajustar `spring.cache.caffeine.spec` según tamaño real.
   - Limpiar caches manualmente con `curl -X POST /actuator/caches/{cacheName}` si se requiere.

---

Este plan reduce significativamente el tiempo de respuesta y la carga del servidor; puede completarse en ~30 minutos.
