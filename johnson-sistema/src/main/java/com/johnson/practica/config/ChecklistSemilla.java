package com.johnson.practica.config;

import com.johnson.practica.model.CatalogoElemento;
import com.johnson.practica.repositorio.CatalogoElementoRepositorio;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ChecklistSemilla {

    @Autowired
    private ElementoChecklistRepositorio elementoRepo;

    @Bean
    CommandLineRunner initDatabase(CatalogoElementoRepositorio repositorio) {
        return args -> {
            try {
                // 1. Limpieza segura (Hijos primero, luego Padres)
                elementoRepo.deleteAll();
                repositorio.deleteAll();

                // 2. Cargas
                cargarProgramaAPQP(repositorio); // Tu lista manual original
                cargarStage2(repositorio);       // Checklist Detallado
                
                // Gate Reviews (Simples)
                cargarGateReview(repositorio, "3. Stage 3");
                cargarGateReview(repositorio, "4. Stage 4");
                cargarGateReview(repositorio, "5. Stage 5");

                System.out.println("✅ SEMILLA CARGADA: Estructura original restaurada y Stages configurados.");
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }
        };
    }

    // --- TU LISTA MANUAL ORIGINAL (Programa Maestro) ---
    private void cargarProgramaAPQP(CatalogoElementoRepositorio repo) {
        List<CatalogoElemento> programa = Arrays.asList(
            // ETAPA 2
            crearE("PROJ", "ETAPA 2", "1. Equipo multifuncional / CFT Kick-off"),
            crearE("DE",   "ETAPA 2", "2. DFMEA Completo"),
            crearE("DE",   "ETAPA 2", "3. Lista preliminar de materiales (BOM)"),
            crearE("DE",   "ETAPA 2", "4. Dibujos de Ingeniería (Drawings)"),
            crearE("QE",   "ETAPA 2", "5. Listas de equipo / Equipment lists"),
            crearE("PROJ", "ETAPA 2", "6. Compromiso de factibilidad (Team Feasibility)"),
            crearE("ALL",  "ETAPA 2", "7. Lecciones aprendidas (Proyectos similares)"), // El cambio a ALL
            crearE("PROJ", "ETAPA 2", "8. Plan de Proyecto y Timing Chart"),
            // ETAPA 3
            crearE("QE",   "ETAPA 3", "9. Revisión del sistema de calidad"),
            crearE("PE",   "ETAPA 3", "10. Diagrama de flujo de proceso"),
            crearE("PE",   "ETAPA 3", "11. Plano de distribución de planta"),
            crearE("PE",   "ETAPA 3", "12. Matriz de características"),
            crearE("PE",   "ETAPA 3", "13. AMEF de Proceso / PFMEA"),
            crearE("QE",   "ETAPA 3", "14. Plan de Control de Pre-Lanzamiento"),
            crearE("PE",   "ETAPA 3", "15. Instrucciones de Proceso / WI"),
            crearE("QE",   "ETAPA 3", "16. Plan de MSA"),
            crearE("QE",   "ETAPA 3", "17. Plan de SPC"),
            // ETAPA 4
            crearE("OPS",  "ETAPA 4", "18. Corrida significativa (Run @ Rate)"),
            crearE("QE",   "ETAPA 4", "19. Resultados de MSA"),
            crearE("QE",   "ETAPA 4", "20. Estudio de habilidad (Ppk)"),
            crearE("QE",   "ETAPA 4", "21. Aprobación PPAP / PSW"),
            crearE("QE",   "ETAPA 4", "22. Pruebas de Validación (PV)"),
            crearE("PE",   "ETAPA 4", "23. Evaluación de Empaque"),
            crearE("QE",   "ETAPA 4", "24. Plan de Control de Producción"),
            crearE("QE",   "ETAPA 4", "25. Cierre de planeación de calidad"),
            // ETAPA 5
            crearE("PROJ", "ETAPA 5", "Inicio de Producción (SOP)")
        );
        repo.saveAll(programa);
    }

    // --- STAGE 2: CHECKLIST DETALLADO (COMPLETO 23 PUNTOS) ---
    private void cargarStage2(CatalogoElementoRepositorio repo) {
        List<CatalogoElemento> stage2 = Arrays.asList(
            // --- Información Preliminar ---
            crearDetalle("S2-01", "¿Se cuenta con un CFT completado?", "2. Stage 2", "Inf. Preliminar", "Project Engineer"),
            crearDetalle("S2-02", "¿Se cuenta con el DFMEA completo?", "2. Stage 2", "Inf. Preliminar", "Design Engineer"),
            crearDetalle("S2-03", "¿Se cuenta con un BOM Preliminar?", "2. Stage 2", "Inf. Preliminar", "Design Engineer"),
            crearDetalle("S2-04", "¿Los dibujos están disponibles?", "2. Stage 2", "Inf. Preliminar", "Design Engineer"),
            crearDetalle("S2-05", "¿Listas de equipo disponibles?", "2. Stage 2", "Inf. Preliminar", "QE/PE"),
            crearDetalle("S2-06", "¿Team Feasibility Commitment firmado?", "2. Stage 2", "Inf. Preliminar", "Project Engineer"),
            crearDetalle("S2-07", "¿Lecciones aprendidas documentadas?", "2. Stage 2", "Inf. Preliminar", "QE/PE"),
            crearDetalle("S2-08", "¿Manual de Proveedor disponible?", "2. Stage 2", "Inf. Preliminar", "Project Engineer"),
            
            // --- LOS QUE FALTABAN (09 - 15) ---
            crearDetalle("S2-09", "¿Se proporcionó el Reporte DV?", "2. Stage 2", "Inf. Preliminar", "Project Engineer"),
            crearDetalle("S2-10", "¿Plan de validación de diseño (DVP)?", "2. Stage 2", "Inf. Preliminar", "Design Engineer"),
            crearDetalle("S2-11", "¿Plan de validación de proceso (PVP)?", "2. Stage 2", "Inf. Preliminar", "Quality Engineer"),
            crearDetalle("S2-12", "¿Plan de control preliminar?", "2. Stage 2", "Inf. Preliminar", "Quality Engineer"),
            crearDetalle("S2-13", "¿Diagrama de flujo de proceso preliminar?", "2. Stage 2", "Inf. Preliminar", "Process Engineer"),
            crearDetalle("S2-14", "¿Layout preliminar?", "2. Stage 2", "Inf. Preliminar", "Process Engineer"),
            crearDetalle("S2-15", "¿Plan de empaque preliminar?", "2. Stage 2", "Inf. Preliminar", "Process Engineer"),

            // --- Abastecimiento (Sourcing) ---
            crearDetalle("S2-16", "¿Lista alineada con RFQ tracker?", "2. Stage 2", "Abastecimiento", "SCS Procurement"),
            crearDetalle("S2-17", "¿Proveedores de nuevos materiales conocidos?", "2. Stage 2", "Abastecimiento", "SCS Procurement"),
            crearDetalle("S2-18", "¿Características especiales identificadas?", "2. Stage 2", "Abastecimiento", "Project Engineer"),
            crearDetalle("S2-19", "¿Se consideró el tiempo de entrega (Lead Time)?", "2. Stage 2", "Abastecimiento", "SCS Procurement"),
            crearDetalle("S2-20", "¿QRs para nuevos componentes disponibles?", "2. Stage 2", "Abastecimiento", "SCS Procurement"),
            crearDetalle("S2-21", "¿TP de nuevos componentes disponibles?", "2. Stage 2", "Abastecimiento", "Finance Rep"),

            // --- Requerimientos del Cliente ---
            crearDetalle("S2-22", "¿Lista preliminar de características del cliente?", "2. Stage 2", "Req. Cliente", "Design Engineer"),
            crearDetalle("S2-23", "¿Lista avalada por firma del cliente?", "2. Stage 2", "Req. Cliente", "Design Engineer")
        );
        repo.saveAll(stage2);
    }

    
    private void cargarGateReview(CatalogoElementoRepositorio repo, String fase) {
        List<CatalogoElemento> gate = Arrays.asList(
            // --- SUBSECCIÓN 1: VALIDACIÓN (PREGUNTAS DE ARRIBA) ---
            crearGate("GATE-01", "¿Todos los puntos del APQP Checklist están cerrados?", fase, "Validación"),
            crearGate("GATE-02", "¿Entregables validados y auditados por el equipo?", fase, "Validación"),
            crearGate("GATE-03", "¿Entregables completados en tiempo?", fase, "Validación"),

            // --- SUBSECCIÓN 2: CONCLUSIÓN (TABLA DE ABAJO) ---
            crearGate("CONC-01", "CIERRE / CLOSE: El proyecto puede cerrarse.", fase, "Conclusión"),
            crearGate("CONC-02", "DESVIACIÓN / DEVIATION: Situaciones menores abiertas.", fase, "Conclusión"),
            crearGate("CONC-03", "ABIERTO / OPEN: Evidencia insuficiente.", fase, "Conclusión")
        );
        repo.saveAll(gate);
    }

    
    // --- HELPERS ---
    private CatalogoElemento crearE(String champ, String etapa, String nom) {
        CatalogoElemento e = new CatalogoElemento();
        e.setNombre(nom); e.setFase("0. Programa"); e.setChampion(champ);
        e.setEtapaVisual(etapa); e.setTipoInput("HITO"); e.setGrupo("Master Plan");
        return e;
    }

    private CatalogoElemento crearDetalle(String cod, String nom, String fase, String grupo, String resp) {
        CatalogoElemento e = new CatalogoElemento();
        e.setCodigo(cod); e.setNombre(nom); e.setFase(fase); e.setGrupo(grupo);
        e.setChampion(resp); e.setTipoInput("PREGUNTA"); // Activará Si/No/NA
        return e;
    }

    private CatalogoElemento crearGate(String cod, String nom, String fase, String grupo) {
        CatalogoElemento e = new CatalogoElemento();
        e.setCodigo(cod); e.setNombre(nom); e.setFase(fase); e.setGrupo(grupo);
        e.setChampion("N/A"); // No lleva responsable por fila en el Gate
        e.setTipoInput("GATE"); // Tipo especial para tabla simple
        return e;
    }
}