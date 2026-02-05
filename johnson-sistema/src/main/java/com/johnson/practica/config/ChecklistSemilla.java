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
            //repositorio.deleteAll(); 

            if (repositorio.count() == 0) {
                
                // ==============================================================================
                // PESTAÑA 1: PROGRAMA APQP (Hitos Clave del Proyecto)
                // ==============================================================================
                List<CatalogoElemento> programa = Arrays.asList(
                    crear("HITO-01", "Equipo multifuncional / CFT (Kick-off)", "0. Programa", "Hito"),
                    crear("HITO-02", "Plan de Control de Producción / Production Control Plan", "0. Programa", "Hito"),
                    crear("HITO-03", "Disposición de piso / Floor plan layout", "0. Programa", "Hito"),
                    crear("HITO-04", "Firma de entrega / Inicio de producción (SOP)", "0. Programa", "Hito Crítico"),
                    crear("HITO-05", "Reducción de variación / Reduced Variation", "0. Programa", "KPI"),
                    crear("HITO-06", "Mejora en satisfacción del cliente / Improve satisfaction", "0. Programa", "KPI"),
                    crear("HITO-07", "Aprobación PPAP (PSW Firmado)", "0. Programa", "Hito Crítico")
                );
                repositorio.saveAll(programa);

                // ==============================================================================
                // PESTAÑA 2: STAGE 2 (Diseño y Desarrollo) - Lista Completa de 23 Puntos
                // ==============================================================================
                List<CatalogoElemento> stage2 = Arrays.asList(
                    
                    // --- Grupo 1: Información Preliminar ---
                    crear("S2-01", "¿Se cuenta con un CFT completado? / Is there a CFT completed?", "2. Stage 2", "1. Preliminar"),
                    crear("S2-02", "¿Se cuenta con el DFMEA completo? / Is there a DFMEA completed?", "2. Stage 2", "1. Preliminar"),
                    crear("S2-03", "¿Se cuenta con un BOM Preliminar? / Is there a Preliminary BOM?", "2. Stage 2", "1. Preliminar"),
                    crear("S2-04", "¿Los dibujos están disponibles? / Are the drawings available?", "2. Stage 2", "1. Preliminar"),
                    crear("S2-05", "¿Se cuentan con las listas del equipo disponibles? / Are equipment lists available?", "2. Stage 2", "1. Preliminar"),
                    crear("S2-06", "¿Team Feasibility Commitment firmado? / Is Team Feasibility signed?", "2. Stage 2", "1. Preliminar"),
                    crear("S2-07", "¿Hay lecciones aprendidas documentadas? / Are lessons learned documented?", "2. Stage 2", "1. Preliminar"),
                    crear("S2-08", "¿Se cuenta con el Manual de Proveedor? / Is there a Supplier Manual?", "2. Stage 2", "1. Preliminar"),
                    crear("S2-09", "¿Se proporcionó el Reporte DV? / Was The DV Report provided?", "2. Stage 2", "1. Preliminar"),

                    // --- Grupo 2: Diseño de Producto ---
                    crear("S2-10", "¿La matriz de características está disponible? / Characteristics Matrix available?", "2. Stage 2", "2. Diseño Producto"),
                    crear("S2-11", "¿Se cuenta con el Plan de Control de Prototipo? / Control Plan Prototype?", "2. Stage 2", "2. Diseño Producto"),
                    crear("S2-12", "¿La construcción de Prototipos fue realizada? / Prototype Build?", "2. Stage 2", "2. Diseño Producto"),
                    crear("S2-13", "¿Dibujos y especificaciones de materiales disponibles?", "2. Stage 2", "2. Diseño Producto"),
                    crear("S2-14", "¿Validación de cambios de ingeniería realizada?", "2. Stage 2", "2. Diseño Producto"),
                    crear("S2-15", "¿Requerimientos de herramental y equipo definidos?", "2. Stage 2", "2. Diseño Producto"),

                    // --- Grupo 3: Materiales y Compras (SCS) ---
                    crear("S2-16", "¿Lista alineada con el RFQ tracker de procuramiento?", "2. Stage 2", "3. Materiales (SCS)"),
                    crear("S2-17", "¿Se conocen los proveedores de los nuevos materiales?", "2. Stage 2", "3. Materiales (SCS)"),
                    crear("S2-18", "¿Las características especiales del material son identificadas?", "2. Stage 2", "3. Materiales (SCS)"),
                    crear("S2-19", "¿Se ha considerado el tiempo de entrega (Lead Time)?", "2. Stage 2", "3. Materiales (SCS)"),
                    crear("S2-20", "¿Se cuentan con todas las QRs aplicables para nuevos componentes?", "2. Stage 2", "3. Materiales (SCS)"),
                    crear("S2-21", "¿Los TP de nuevos componentes están disponibles?", "2. Stage 2", "3. Materiales (SCS)"),

                    // --- Grupo 4: Requisitos Cliente ---
                    crear("S2-22", "¿Lista preliminar de características del cliente disponible?", "2. Stage 2", "4. Cliente"),
                    crear("S2-23", "¿Esta lista se encuentra avalada por la firma del cliente?", "2. Stage 2", "4. Cliente")
                );
                repositorio.saveAll(stage2);

                System.out.println("✅ Base de datos actualizada con Checklist F7100 Correcto (23 Puntos).");
            }
        };
    }

    // Método auxiliar para crear elementos del catálogo
    private CatalogoElemento crear(String codigo, String nombre, String fase, String categoria) {
        CatalogoElemento elemento = new CatalogoElemento();
        elemento.setCodigo(codigo);
        elemento.setNombre(nombre); // Nombre exacto del PDF/Excel
        elemento.setFase(fase);     // Para filtrar por Pestaña
        elemento.setTipo(categoria); // Para agrupar en Acordeón/Tabla
        elemento.setRequerido(true);
        return elemento;
    }
}