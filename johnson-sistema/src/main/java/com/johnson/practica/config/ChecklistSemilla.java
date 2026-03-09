package com.johnson.practica.config;

import com.johnson.practica.modelo.CatalogoElemento;
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

                System.out.println("SEED LOADED: Original structure restored, Stage 1 added and Stages configured.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        };
    }

    private void cargarProgramaAPQP(CatalogoElementoRepositorio repo) {
        List<CatalogoElemento> programa = Arrays.asList(
            // --- STAGE 1 --
            crearE("1.1", "QE",    "STAGE 1", "CFT Planning and Definition"),
            crearE("1.2", "DE", "STAGE 1", "Project Scope Definition"),
            crearE("1.3", "DE", "STAGE 1", "Establish 'Team to Team' meetings"),
            crearE("1.4", "ALL",   "STAGE 1", "Training (Identify training needs)"),
            crearE("1.5", "DE",                       "STAGE 1", "Concerns/Issues Resolution"),
            crearE("1.6", "DE", "STAGE 1", "Timeline / Gantt"),

            // --- STAGE 2 ---
            crearE("P-01", "PROJ",   "STAGE 2", "Cross-Functional Team / CFT (JE Global and internal):"),
            crearE("P-02", "DE",     "STAGE 2", "DFMEA:"),
            crearE("P-03", "DE",     "STAGE 2", "Preliminary BOM:"),
            crearE("P-04", "DE",     "STAGE 2", "Drawings:"),
            crearE("P-05", "QE/PE",  "STAGE 2", "List of equipment, fixtures, tools, spare parts, gauges, and facilities needed"),
            crearE("P-06", "PROJ",   "STAGE 2", "Team Feasibility Commitment:"),
            crearE("P-07", "ALL",    "STAGE 2", "Lessons learned."),
            crearE("P-08", "PROJ",   "STAGE 2", "Customer Supplier Manual"),
            crearE("P-09", "DE",     "STAGE 2", "Design Validation Report / DV Report"),
            crearE("P-10", "QE",     "STAGE 2", "Preliminary Customer Characteristics List:"),

            // --- STAGE 3 ---
            crearE("P-11", "QE",     "STAGE 3", "01.- Packaging Specifications"),
            crearE("P-12", "PE",     "STAGE 3", "01.- Packaging Specifications"), 
            crearE("P-13", "QE",     "STAGE 3", "02.- QMS Changes for product manufacturing"),
            crearE("P-14", "PE",     "STAGE 3", "03.- Process Flow Chart"),
            crearE("P-15", "PE",     "STAGE 3", "04.- Floor plan layout"),
            crearE("P-16", "QE/PE",  "STAGE 3", "05.- Characteristic Matrix"),
            crearE("P-17", "PE",     "STAGE 3", "0.6- PFMEA"),
            crearE("P-18", "QE",     "STAGE 3", "0.7- Control Plan Pre-launch"),
            crearE("P-19", "PE",     "STAGE 3", "0.8- WI"),
            crearE("P-20", "QE",     "STAGE 3", "0.9- MSA Plan"),
            crearE("P-21", "QE/PE",  "STAGE 3", "10.- SPC Plan"),
            crearE("P-22", "PROJ",   "STAGE 3", "11.- Meeting Minutes"),
            crearE("P-23", "QE",     "STAGE 3", "11.- Meeting Minutes"), 
            crearE("P-24", "PROJ",   "STAGE 3", "Stage Revision (line in Mexico):"),

            // --- STAGE 4 ---
            crearE("P-25", "PROJ",   "STAGE 4", "12.- Pilot Run"),
            crearE("P-26", "QE",     "STAGE 4", "13.- MSA"),
            crearE("P-27", "QE/PE",  "STAGE 4", "14.- Preliminary SPC"),
            crearE("P-28", "QE",     "STAGE 4", "15.- PPAP"),
            crearE("P-29", "QE",     "STAGE 4", "16.- Production Validation Testing"),
            crearE("P-30", "PE",     "STAGE 4", "17.- Packaging evaluation"),
            crearE("P-31", "QE",     "STAGE 4", "18.- Production Control Plan"),
            crearE("P-32", "PE",     "STAGE 4", "04.- Floor plan layout"), 
            crearE("P-33", "PROJ",   "STAGE 4", "19.- Sign-OFF"),

            // --- STAGE 5 ---
            crearE("P-34", "QE/PE",  "STAGE 5", "20.- Reduced Variation"),
            crearE("P-35", "QE",     "STAGE 5", "21.- Improve customer satisfaction"),
            crearE("P-36", "QE",     "STAGE 5", "22.- Improved delivery and service"),
            crearE("P-37", "QE/PE",  "STAGE 5", "23.- Effective use of Lesson Learned/Best practice"),
            crearE("P-38", "PROJ",   "STAGE 5", "25.- Formal project delivery to production:")
        );
        repo.saveAll(programa);
    }

    // --- STAGE 2
    private void cargarStage2(CatalogoElementoRepositorio repo) {
        List<CatalogoElemento> stage2 = Arrays.asList(
            crearDetalle("S2-01", "Is a completed CFT available?", "2. Stage 2", "Preliminary information", "Project Engineer"),
            crearDetalle("S2-02", "Is the complete DFMEA available?", "2. Stage 2", "Preliminary information", "Design Engineer"),
            crearDetalle("S2-03", "Is a Preliminary BOM available?", "2. Stage 2", "Preliminary information", "Design Engineer"),
            crearDetalle("S2-04", "Are the drawings available?", "2. Stage 2", "Preliminary information", "Design Engineer"),
            crearDetalle("S2-05", "Are equipment lists available?", "2. Stage 2", "Preliminary information", "QE/PE"),
            crearDetalle("S2-06", "Is the Team Feasibility Commitment signed?", "2. Stage 2", "Preliminary information", "Project Engineer"),
            crearDetalle("S2-07", "Are lessons learned documented?", "2. Stage 2", "Preliminary information", "QE/PE"),
            crearDetalle("S2-08", "Is the Supplier Manual available?", "2. Stage 2", "Preliminary information", "Project Engineer"),
            
            crearDetalle("S2-09", "Was the DV Report provided?", "2. Stage 2", "Preliminary information", "Project Engineer"),
            crearDetalle("S2-10", "Design Validation Plan (DVP)?", "2. Stage 2", "Preliminary information", "Design Engineer"),
            crearDetalle("S2-11", "Process Validation Plan (PVP)?", "2. Stage 2", "Engineering Drawings", "Quality Engineer"),
            crearDetalle("S2-12", "Preliminary Control Plan?", "2. Stage 2", "Engineering Drawings", "Quality Engineer"),
            crearDetalle("S2-13", "Preliminary Process Flow Diagram?", "2. Stage 2", "Engineering Drawings", "Process Engineer"),
            crearDetalle("S2-14", "Preliminary Layout?", "2. Stage 2", "Engineering Drawings", "Process Engineer"),
            crearDetalle("S2-15", "Preliminary Packaging Plan?", "2. Stage 2", "New components", "Process Engineer"),

            crearDetalle("S2-16", "Is the list aligned with the RFQ tracker?", "2. Stage 2", "New components", "SCS Procurement"),
            crearDetalle("S2-17", "Are the suppliers of new materials known?", "2. Stage 2", "New components", "SCS Procurement"),
            crearDetalle("S2-18", "Are special characteristics identified?", "2. Stage 2", "New components", "Project Engineer"),
            crearDetalle("S2-19", "Was the lead time considered?", "2. Stage 2", "New components", "SCS Procurement"),
            crearDetalle("S2-20", "Are QRs for new components available?", "2. Stage 2", "New components", "SCS Procurement"),
            crearDetalle("S2-21", "Are TP for new components available?", "2. Stage 2", "New components", "Finance Rep"),

            crearDetalle("S2-22", "Preliminary list of customer characteristics?", "2. Stage 2", "Preliminary Customer Characteristics List:", "Design Engineer"),
            crearDetalle("S2-23", "Is the list endorsed by the customer's signature?", "2. Stage 2", "Preliminary Customer Characteristics List:", "Design Engineer")
        );
        repo.saveAll(stage2);
    }

    private void cargarGateReview(CatalogoElementoRepositorio repo, String fase) {
        List<CatalogoElemento> gate = Arrays.asList(
            // VALIDATION 
            crearGate("GATE-01", "Are all APQP Checklist items closed?", fase, "Validation"),
            crearGate("GATE-02", "Are deliverables validated and audited by the team?", fase, "Validation"),
            crearGate("GATE-03", "Were deliverables completed on time?", fase, "Validation"),

            // CONCLUSION
            crearGate("CONC-01", "CLOSE: The project can be closed.", fase, "Conclusion"),
            crearGate("CONC-02", "DEVIATION: Minor open situations.", fase, "Conclusion"),
            crearGate("CONC-03", "OPEN: Insufficient evidence.", fase, "Conclusion")
        );
        repo.saveAll(gate);
    }

    // --- HELPERS ---
    private CatalogoElemento crearE(String cod, String champ, String etapa, String nom) {
        CatalogoElemento e = new CatalogoElemento();
        e.setCodigo(cod);
        e.setNombre(nom); e.setFase("0. Program"); e.setChampion(champ);
        e.setEtapaVisual(etapa); e.setTipoInput("HITO"); e.setGrupo("Master Plan");
        return e;
    }

    private CatalogoElemento crearDetalle(String cod, String nom, String fase, String grupo, String resp) {
        CatalogoElemento e = new CatalogoElemento();
        e.setCodigo(cod); e.setNombre(nom); e.setFase(fase); e.setGrupo(grupo);
        e.setChampion(resp); e.setTipoInput("PREGUNTA"); 
        return e;
    }

    private CatalogoElemento crearGate(String cod, String nom, String fase, String grupo) {
        CatalogoElemento e = new CatalogoElemento();
        e.setCodigo(cod); e.setNombre(nom); e.setFase(fase); e.setGrupo(grupo);
        e.setChampion("N/A"); 
        e.setTipoInput("GATE"); 
        return e;
    }
}