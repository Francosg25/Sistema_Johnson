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
            // Limpieza inicial para evitar duplicados
            repositorio.deleteAll();

            if (repositorio.count() == 0) {
                cargarProgramaAPQP(repositorio);
                cargarStage2(repositorio);
                
                System.out.println("✅ Base de datos inicializada con Módulos separados (Hitos y Preguntas).");
            }
        };
    }

    // --- MÓDULO 1: PROGRAMA APQP (Hitos con Fechas) ---
    private void cargarProgramaAPQP(CatalogoElementoRepositorio repo) {
        List<CatalogoElemento> hitos = Arrays.asList(
            crear("HITO-01", "Equipo multifuncional / CFT (Kick-off)", "0. Programa", "HITO", "Inicio"),
            crear("HITO-02", "DFMEA Completo", "0. Programa", "HITO", "Ingeniería"),
            crear("HITO-03", "Lista preliminar de materiales / Preliminary BOM", "0. Programa", "HITO", "Ingeniería"),
            crear("HITO-04", "Dibujos Disponibles / Drawings", "0. Programa", "HITO", "Ingeniería"),
            crear("HITO-05", "Plan de Control de Producción / Production Control Plan", "0. Programa", "HITO", "Calidad"),
            crear("HITO-06", "Disposición de piso / Floor plan layout", "0. Programa", "HITO", "Manufactura"),
            crear("HITO-07", "Inicio de producción (SOP) / Sign-OFF", "0. Programa", "HITO", "Crítico"),
            crear("HITO-08", "Reducción de variación / Reduced Variation", "0. Programa", "HITO", "KPI"),
            crear("HITO-09", "Mejora en satisfacción del cliente / Improve satisfaction", "0. Programa", "HITO", "KPI"),
            crear("HITO-10", "Aprobación PPAP (PSW Firmado)", "0. Programa", "HITO", "Crítico")
        );
        repo.saveAll(hitos);
    }

    // --- MÓDULO 2: STAGE 2 (Checklist de Validación Técnica) ---
    // Fuente: F7100 APQP Checklist - Stage2.csv
    private void cargarStage2(CatalogoElementoRepositorio repo) {
        List<CatalogoElemento> stage2 = Arrays.asList(
            
            // GRUPO 1: Información Preliminar
            crear("S2-01", "¿Se cuenta con un CFT completado? / Is there a CFT completed?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),
            crear("S2-02", "¿Se cuenta con el DFMEA completo? / Is there a DFMEA completed?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),
            crear("S2-03", "¿Se cuenta con un BOM Preliminar? / Is there a Preliminary BOM?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),
            crear("S2-04", "¿Los dibujos están disponibles? / Are the drawings available?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),
            crear("S2-05", "¿Se cuentan con las listas del equipo disponibles? / Are equipment lists available?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),
            crear("S2-06", "¿Team Feasibility Commitment firmado? / Is Team Feasibility signed?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),
            crear("S2-07", "¿Hay lecciones aprendidas documentadas? / Are lessons learned documented?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),
            crear("S2-08", "¿Se cuenta con el Manual de Proveedor? / Is there a Supplier Manual?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),
            crear("S2-09", "¿Se proporcionó el Reporte DV? / Was The DV Report provided?", "2. Stage 2", "PREGUNTA", "1. Información Preliminar"),

            // GRUPO 2: Diseño de Producto
            crear("S2-10", "¿La matriz de características está disponible? / Characteristics Matrix available?", "2. Stage 2", "PREGUNTA", "2. Diseño Producto"),
            crear("S2-11", "¿Se cuenta con el Plan de Control de Prototipo? / Control Plan Prototype?", "2. Stage 2", "PREGUNTA", "2. Diseño Producto"),
            crear("S2-12", "¿La construcción de Prototipos fue realizada? / Prototype Build?", "2. Stage 2", "PREGUNTA", "2. Diseño Producto"),
            crear("S2-13", "¿Dibujos y especificaciones de materiales disponibles?", "2. Stage 2", "PREGUNTA", "2. Diseño Producto"),
            crear("S2-14", "¿Validación de cambios de ingeniería realizada?", "2. Stage 2", "PREGUNTA", "2. Diseño Producto"),
            crear("S2-15", "¿Requerimientos de herramental y equipo definidos?", "2. Stage 2", "PREGUNTA", "2. Diseño Producto"),

            // GRUPO 3: Materiales y Compras (SCS)
            crear("S2-16", "¿Lista alineada con el RFQ tracker de procuramiento?", "2. Stage 2", "PREGUNTA", "3. Materiales (SCS)"),
            crear("S2-17", "¿Se conocen los proveedores de los nuevos materiales?", "2. Stage 2", "PREGUNTA", "3. Materiales (SCS)"),
            crear("S2-18", "¿Las características especiales del material son identificadas?", "2. Stage 2", "PREGUNTA", "3. Materiales (SCS)"),
            crear("S2-19", "¿Se ha considerado el tiempo de entrega (Lead Time)?", "2. Stage 2", "PREGUNTA", "3. Materiales (SCS)"),
            crear("S2-20", "¿Se cuentan con todas las QRs aplicables para nuevos componentes?", "2. Stage 2", "PREGUNTA", "3. Materiales (SCS)"),
            crear("S2-21", "¿Los TP de nuevos componentes están disponibles?", "2. Stage 2", "PREGUNTA", "3. Materiales (SCS)"),

            // GRUPO 4: Requisitos Cliente
            crear("S2-22", "¿Lista preliminar de características del cliente disponible?", "2. Stage 2", "PREGUNTA", "4. Cliente"),
            crear("S2-23", "¿Esta lista se encuentra avalada por la firma del cliente?", "2. Stage 2", "PREGUNTA", "4. Cliente")
        );
        repo.saveAll(stage2);
    }

    // Método factoría actualizado con los nuevos campos
    private CatalogoElemento crear(String codigo, String nombre, String fase, String tipoInput, String grupo) {
        CatalogoElemento elemento = new CatalogoElemento();
        elemento.setCodigo(codigo);
        elemento.setNombre(nombre);
        elemento.setFase(fase);
        elemento.setTipoInput(tipoInput); // "HITO" o "PREGUNTA"
        elemento.setGrupo(grupo);         // Para agrupar visualmente después
        elemento.setRequerido(true);
        return elemento;
    }
}