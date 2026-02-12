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
                elementoRepo.deleteAll();
                repositorio.deleteAll();

                cargarProgramaAPQP(repositorio); 
                cargarStage2(repositorio);       
                
                cargarGateReview(repositorio, "3. Stage 3");
                cargarGateReview(repositorio, "4. Stage 4");
                cargarGateReview(repositorio, "5. Stage 5");

                System.out.println("SEMILLA CARGADA: Estructura original restaurada y Stages configurados.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        };
    }

    private void cargarProgramaAPQP(CatalogoElementoRepositorio repo) {
        List<CatalogoElemento> programa = Arrays.asList(
            // ETAPA 2
            crearE("P-01", "PROJ",   "ETAPA 2", "Equipo multifuncional / CFT (JE Global and internal):"),
            crearE("P-02", "DE",     "ETAPA 2", "DFMEA:"),
            crearE("P-03", "DE",     "ETAPA 2", "Lista preliminar de materiales / Preliminary BOM:"),
            crearE("P-04", "DE",     "ETAPA 2", "Dibujos / Drawings:"),
            crearE("P-05", "QE/PE",  "ETAPA 2", "Lista de equipos, fixturas, herramietas, refacciones,gauges e instalaciones necesarias"),
            crearE("P-06", "PROJ",   "ETAPA 2", "Compromiso de Factibilidad del Equipo / Team Feasibility Commitment:"),
            crearE("P-07", "ALL",    "ETAPA 2", "Lecciones aprendidas/ Lessons learned."),
            crearE("P-08", "PROJ",   "ETAPA 2", "Manual de Proveedor / Customer Supplier Manual"),
            crearE("P-09", "DE",     "ETAPA 2", "Reporte de Validación del Diseño / DV Report"),
            crearE("P-10", "QE",     "ETAPA 2", "Lista preliminar de características del cliente / Preliminary Customer Characteristics List:"),

            // ETAPA 3
            crearE("P-11", "QE",     "ETAPA 3", "01.- Especificaciones de empaque"),
            crearE("P-12", "PE",     "ETAPA 3", "01.- Especificaciones de empaque"), // Duplicado según excel
            crearE("P-13", "QE",     "ETAPA 3", "02.-Cambios en el SGC para fabricar el producto"),
            crearE("P-14", "PE",     "ETAPA 3", "03.-Process Flow Chart"),
            crearE("P-15", "PE",     "ETAPA 3", "04.-Floor plan layout"),
            crearE("P-16", "QE/PE",  "ETAPA 3", "05.-Characteristic Matrix"),
            crearE("P-17", "PE",     "ETAPA 3", "0.6- PFMEA"),
            crearE("P-18", "QE",     "ETAPA 3", "0.7-Control Plan Pre-launch"),
            crearE("P-19", "PE",     "ETAPA 3", "0.8- WI"),
            crearE("P-20", "QE",     "ETAPA 3", "0.9- MSA Plan"),
            crearE("P-21", "QE/PE",  "ETAPA 3", "10.-SPC Plan"),
            crearE("P-22", "PROJ",   "ETAPA 3", "11.- Minutas Juntas"),
            crearE("P-23", "QE",     "ETAPA 3", "11.- Minutas Juntas"), // Duplicado según excel
            crearE("P-24", "PROJ",   "ETAPA 3", "Revisión de etapa (línea en México) / Stage Revision (line in Mexico):"),

            // ETAPA 4
            crearE("P-25", "PROJ",   "ETAPA 4", "12.- Pilot Run"),
            crearE("P-26", "QE",     "ETAPA 4", "13.- MSA"),
            crearE("P-27", "QE/PE",  "ETAPA 4", "14.-Preliminary SPC"),
            crearE("P-28", "QE",     "ETAPA 4", "15.- PPAP"),
            crearE("P-29", "QE",     "ETAPA 4", "16.-Production Validation Testing"),
            crearE("P-30", "PE",     "ETAPA 4", "17.-Packaging evaluation"),
            crearE("P-31", "QE",     "ETAPA 4", "18.-Production Control Plan"),
            crearE("P-32", "PE",     "ETAPA 4", "04.-Floor plan layout"), // Duplicado según excel
            crearE("P-33", "PROJ",   "ETAPA 4", "19.- Sign-OFF"),

            // ETAPA 5
            crearE("P-34", "QE/PE",  "ETAPA 5", "20.-Reduced Variation"),
            crearE("P-35", "QE",     "ETAPA 5", "21.-Improve customer satisfaction"),
            crearE("P-36", "QE",     "ETAPA 5", "22.- Improved delivery and service"),
            crearE("P-37", "QE/PE",  "ETAPA 5", "23.- Effective use of Lesson Learned/Best practice"),
            crearE("P-38", "PROJ",   "ETAPA 5", "25.- Entrega formal de proyecto a producción / Formal project delivery to production:")
        );
        repo.saveAll(programa);
    }

    // --- STAGE 2: CHECKLIST DETALLADO (COMPLETO 23 PUNTOS) ---
    private void cargarStage2(CatalogoElementoRepositorio repo) {
        List<CatalogoElemento> stage2 = Arrays.asList(
            crearDetalle("S2-01", "¿Se cuenta con un CFT completado?", "2. Stage 2", "Información preliminar/ Preliminary information", "Project Engineer"),
            crearDetalle("S2-02", "¿Se cuenta con el DFMEA completo?", "2. Stage 2", "Información preliminar/ Preliminary information", "Design Engineer"),
            crearDetalle("S2-03", "¿Se cuenta con un BOM Preliminar?", "2. Stage 2", "Información preliminar/ Preliminary information", "Design Engineer"),
            crearDetalle("S2-04", "¿Los dibujos están disponibles?", "2. Stage 2", "Información preliminar/ Preliminary information", "Design Engineer"),
            crearDetalle("S2-05", "¿Listas de equipo disponibles?", "2. Stage 2", "Información preliminar/ Preliminary information", "QE/PE"),
            crearDetalle("S2-06", "¿Team Feasibility Commitment firmado?", "2. Stage 2", "Información preliminar/ Preliminary information", "Project Engineer"),
            crearDetalle("S2-07", "¿Lecciones aprendidas documentadas?", "2. Stage 2", "Información preliminar/ Preliminary information", "QE/PE"),
            crearDetalle("S2-08", "¿Manual de Proveedor disponible?", "2. Stage 2", "Información preliminar/ Preliminary information", "Project Engineer"),
            
            crearDetalle("S2-09", "¿Se proporcionó el Reporte DV?", "2. Stage 2", "Información preliminar/ Preliminary information", "Project Engineer"),
            crearDetalle("S2-10", "¿Plan de validación de diseño (DVP)?", "2. Stage 2", "Información preliminar/ Preliminary information", "Design Engineer"),
            crearDetalle("S2-11", "¿Plan de validación de proceso (PVP)?", "2. Stage 2", "Dibujos Ingenieriles / Engineering Drawings", "Quality Engineer"),
            crearDetalle("S2-12", "¿Plan de control preliminar?", "2. Stage 2", "Dibujos Ingenieriles / Engineering Drawings", "Quality Engineer"),
            crearDetalle("S2-13", "¿Diagrama de flujo de proceso preliminar?", "2. Stage 2", "Dibujos Ingenieriles / Engineering Drawings", "Process Engineer"),
            crearDetalle("S2-14", "¿Layout preliminar?", "2. Stage 2", "Dibujos Ingenieriles / Engineering Drawings", "Process Engineer"),
            crearDetalle("S2-15", "¿Plan de empaque preliminar?", "2. Stage 2", "Componentes nuevos / New components", "Process Engineer"),

            crearDetalle("S2-16", "¿Lista alineada con RFQ tracker?", "2. Stage 2", "Componentes nuevos / New components", "SCS Procurement"),
            crearDetalle("S2-17", "¿Proveedores de nuevos materiales conocidos?", "2. Stage 2", "Componentes nuevos / New components", "SCS Procurement"),
            crearDetalle("S2-18", "¿Características especiales identificadas?", "2. Stage 2", "Componentes nuevos / New components", "Project Engineer"),
            crearDetalle("S2-19", "¿Se consideró el tiempo de entrega (Lead Time)?", "2. Stage 2", "Componentes nuevos / New components", "SCS Procurement"),
            crearDetalle("S2-20", "¿QRs para nuevos componentes disponibles?", "2. Stage 2", "Componentes nuevos / New components", "SCS Procurement"),
            crearDetalle("S2-21", "¿TP de nuevos componentes disponibles?", "2. Stage 2", "Componentes nuevos / New components", "Finance Rep"),

            crearDetalle("S2-22", "¿Lista preliminar de características del cliente?", "2. Stage 2", "Lista preliminar de características del cliente / Preliminary Customer Characteristics List:", "Design Engineer"),
            crearDetalle("S2-23", "¿Lista avalada por firma del cliente?", "2. Stage 2", "Lista preliminar de características del cliente / Preliminary Customer Characteristics List:", "Design Engineer")
        );
        repo.saveAll(stage2);
    }

    
    private void cargarGateReview(CatalogoElementoRepositorio repo, String fase) {
        List<CatalogoElemento> gate = Arrays.asList(
            // --- SUBSECCIÓN 1: VALIDACIÓN 
            crearGate("GATE-01", "¿Todos los puntos del APQP Checklist están cerrados?", fase, "Validación"),
            crearGate("GATE-02", "¿Entregables validados y auditados por el equipo?", fase, "Validación"),
            crearGate("GATE-03", "¿Entregables completados en tiempo?", fase, "Validación"),

            // --- SUBSECCIÓN 2: CONCLUSIÓN
            crearGate("CONC-01", "CIERRE / CLOSE: El proyecto puede cerrarse.", fase, "Conclusión"),
            crearGate("CONC-02", "DESVIACIÓN / DEVIATION: Situaciones menores abiertas.", fase, "Conclusión"),
            crearGate("CONC-03", "ABIERTO / OPEN: Evidencia insuficiente.", fase, "Conclusión")
        );
        repo.saveAll(gate);
    }

    
    // --- HELPERS ---
    private CatalogoElemento crearE(String cod, String champ, String etapa, String nom) {
        CatalogoElemento e = new CatalogoElemento();
        e.setCodigo(cod);
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