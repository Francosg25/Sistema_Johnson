package com.johnson.practica.controlador;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/api/proxy")
public class ProxyControlador {

    @Value("${EXTERNAL_API_KEY:}")
    private String externalApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/ejemplo-externo")
    public ResponseEntity<String> llamarApiExterna() {
        // En lugar de llamar a la API desde el JavaScript frontal, se hace aquí.
        // La API Key está segura en el servidor/entorno.
        
        String urlExterna = "https://api.ejemplo.com/v1/data";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + externalApiKey);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Descomentar para usar realmente (esto es un ejemplo):
            // return restTemplate.exchange(urlExterna, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok("{\"mensaje\": \"Proxy configurado correctamente. La clave '" + 
                (externalApiKey.isEmpty() ? "VACÍA" : "OCULTA") + "' se usó en el servidor.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error llamando al proxy: " + e.getMessage());
        }
    }
}
