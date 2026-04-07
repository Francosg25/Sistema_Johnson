package com.johnson.practica.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    // Mapa para almacenar cubos (buckets) por IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        
        // Aplicamos rate limiting solo a rutas de autenticación críticas
        if (path.equals("/login") || path.equals("/recuperar-password") || path.equals("/olvido-password")) {
            String ip = getClientIP(httpRequest);
            Bucket bucket = buckets.computeIfAbsent(ip, this::createNewBucket);

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.setContentType("text/plain");
                httpResponse.getWriter().write("Too many login attempts. Please wait a minute.");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private Bucket createNewBucket(String key) {
        // Permitimos 15 peticiones cada 1 minuto (más razonable para evitar falsos positivos)
        return Bucket.builder()
                .addLimit(Bandwidth.classic(15, Refill.greedy(15, Duration.ofMinutes(1))))
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
