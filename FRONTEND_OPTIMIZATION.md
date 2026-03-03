# Optimización Frontend

## Cambios aplicados

- Scripts CDN (`bootstrap.bundle`, `chart.js`) marcados con `defer` y SRI para mejorar seguridad y evitar bloquear el render.
- `/reportes` ahora carga datos vía AJAX (`fetch`) desde `/proyectos/api/reportes/*`.
- Tabla de proyectos en `/` paginada para reducir cantidad de filas transferidas.
- `server.compression.enabled=true` habilita GZIP para respuestas JSON/HTML.
- HTTP/2 activado en `application.properties`.

## Recomendaciones adicionales

1. **Mover estilos CSS a un archivo único** (`/css/style.css`) y minificarlos.
2. **Usar cache-control** en recursos estáticos (`addResourceHandlers`).
3. **Habilitar Service Worker** para PWA y offline.
4. **Lazy load de imágenes** con `loading="lazy"`.
5. **Consultar Web Vitals** con PageSpeed o Lighthouse para afinamientos.

## Cómo comprobar mejoras

- Abrir DevTools > Network > Throttling (Slow 3G) y refrescar.
- Medir `Time to Interactive` y `Largest Contentful Paint` antes/después.

