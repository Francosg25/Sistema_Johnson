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
        
        // Aplicamos rate limiting solo a rutas sensibles
        if (path.startsWith("/login") || path.startsWith("/recuperar-password") || path.startsWith("/olvido-password")) {
            String ip = getClientIP(httpRequest);
            Bucket bucket = buckets.computeIfAbsent(ip, this::createNewBucket);

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.setContentType("text/plain");
                httpResponse.getWriter().write("Too many requests. Please try again later.");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private Bucket createNewBucket(String key) {
        // Permitimos 5 peticiones cada 10 minutos para rutas sensibles
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(10))))
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
