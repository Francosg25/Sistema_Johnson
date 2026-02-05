package com.johnson.practica.config;

import com.johnson.practica.model.CatalogoElemento;
import com.johnson.practica.repositorio.CatalogoElementoRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ChecklistSemilla {

    @Bean
    CommandLineRunner initDatabase(CatalogoElementoRepositorio repositorio) {
        return args -> {
            // Solo ejecutamos si la tabla está vacía
            if (repositorio.count() == 0) {
                
                // Lista basada en tu archivo CSV '16.csv'
                List<String> nombresPPAP = Arrays.asList(
                    "1. Registros de Diseño", "2. Cambios de Ingeniería", 
                    "3. Aprobación Ingeniería Cliente", "4. DFMEA", 
                    "5. Diagrama de Flujo", "6. PFMEA", "7. Plan de Control",
                    "8. MSA", "9. Dimensionales", "10. Material / Desempeño",
                    "11. Cpk/Ppk", "12. Laboratorio Calificado", "13. AAR",
                    "14. Muestras Producto", "15. Muestra Maestra", 
                    "16. Ayudas Verificación", "17. Requisitos Cliente", 
                    "18. PSW", "19. Checklist PPAP"
                );

                int contador = 1;
                for (String nombre : nombresPPAP) {
                    CatalogoElemento elemento = new CatalogoElemento();
                    
                    elemento.setCodigo("PPAP-" + String.format("%02d", contador));
                    elemento.setNombre(nombre);
                    elemento.setFase("4. PPAP"); 
                    elemento.setRequerido(true); 
                    
                    repositorio.save(elemento);
                    contador++;
                }
                System.out.println("✅ Catálogo PPAP inicializado correctamente.");
            }
        };
    }
}