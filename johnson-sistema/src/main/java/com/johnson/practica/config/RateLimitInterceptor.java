package com.johnson.practica.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Almacena un "bucket" por cada IP (ideal para entornos locales)
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Definimos el límite: 20 peticiones por minuto (puedes ajustarlo)
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Solo aplicamos el límite a las rutas que empiecen por /api/
        if (request.getRequestURI().startsWith("/api/")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

            if (bucket.tryConsume(1)) {
                return true; // Petición permitida
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("{\"error\": \"Demasiadas peticiones. Intenta de nuevo en un minuto.\"}");
                response.setContentType("application/json");
                return false; // Petición bloqueada
            }
        }
        return true;
    }
}
