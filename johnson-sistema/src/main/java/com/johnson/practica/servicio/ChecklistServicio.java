package com.johnson.practica.servicio;

import com.johnson.practica.dto.FaseVista;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChecklistServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    @Autowired
    private FirmaEtapaServicio firmaEtapaServicio;

    @Autowired
    private ChecklistUpdateServicio checklistUpdateServicio;

    @Autowired
    private ChecklistLogicServicio checklistLogicServicio;

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerHitosPrograma(Long proyectoId) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, "0");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorFase(Long proyectoId, String prefijoFase) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, prefijoFase);
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorProyectoId(Long proyectoId) {
        return repositorio.findByProyecto_IdOrderByCodigoAsc(proyectoId);
    }

    public List<ElementoChecklist> obtenerTodos() {
        return repositorio.findAll();
    }

    @Transactional
    public void guardarChecklistCompleto(Map<String, String> allParams) {
        checklistUpdateServicio.guardarChecklistCompleto(allParams);
    }

    @Transactional(readOnly = true)
    public List<FaseVista> construirFasesVista(Long proyectoId) {
        List<ElementoChecklist> todosLosElementos = obtenerPorProyectoId(proyectoId);
        List<FaseVista> fases = new ArrayList<>();
        
        fases.add(new FaseVista("prog", "APQP Program", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("0")).toList(), new HashMap<>()));
                
        fases.add(new FaseVista("s2", "Stage 2", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("2")).toList(), new HashMap<>()));
                
        fases.add(new FaseVista("s3", "Stage 3", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("3")).toList(), 
                firmaEtapaServicio.obtenerFirmasPorEtapa(proyectoId, 3)));
                
        fases.add(new FaseVista("s4", "Stage 4", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("4")).toList(), 
                firmaEtapaServicio.obtenerFirmasPorEtapa(proyectoId, 4)));
                
        fases.add(new FaseVista("s5", "Stage 5", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("5")).toList(), 
                firmaEtapaServicio.obtenerFirmasPorEtapa(proyectoId, 5)));

        return fases;
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerAlertasGlobales() {
        List<ElementoChecklist> todos = repositorio.findAll();
        return todos.stream()
                .filter(e -> e.getProyecto() != null && !e.getProyecto().getEsHistorico()) 
                .filter(e -> e.getControlEntregable() != null && e.getControlEntregable().equalsIgnoreCase("NEEDS ACTION"))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerTareasPendientesUsuario(String username) {
        List<ElementoChecklist> todos = repositorio.findAll();
        return todos.stream()
                .filter(e -> e.getProyecto() != null && !e.getProyecto().getEsHistorico())
                .filter(e -> e.getChampion() != null && 
                            e.getChampion().equalsIgnoreCase(username) && 
                            !"OK".equalsIgnoreCase(scoreFormateado(e.getScore())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerTodasTareasPendientes() {
        return repositorio.findByScoreNotIgnoreCase("OK").stream()
                .filter(e -> e.getProyecto() != null && !e.getProyecto().getEsHistorico())
                .filter(e -> e.getScore() == null || !e.getScore().equalsIgnoreCase("OK"))
                .filter(e -> e.getFase() != null && (e.getFase().equals("0. Program") || e.getFase().equals("2. Stage 2")))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerTodosLosChampions() {
        return repositorio.findDistinctChampions().stream()
                .filter(c -> c != null && !c.trim().isEmpty() && !c.equalsIgnoreCase("N/A"))
                .map(this::normalizarChampion)
                .distinct()
                .sorted()
                .toList();
    }

    public String normalizarChampion(String champ) {
        return checklistLogicServicio.normalizarChampion(champ);
    }

    @Transactional
    public void actualizarEntregablesVencidos() {
        LocalDate hoy = LocalDate.now();
        List<ElementoChecklist> todos = repositorio.findAll();
        List<ElementoChecklist> paraActualizar = new ArrayList<>();

        for (ElementoChecklist e : todos) {
            if (e.getFechaPlan() != null && e.getFechaPlan().isBefore(hoy) 
                && !"OK".equalsIgnoreCase(e.getScore())
                && (e.getControlEntregable() == null || (!e.getControlEntregable().equalsIgnoreCase("Closed late") && !e.getControlEntregable().equalsIgnoreCase("Closed on time")))) {
                
                e.setControlEntregable("Closed late");
                paraActualizar.add(e);
            }
        }

        if (!paraActualizar.isEmpty()) {
            repositorio.saveAll(paraActualizar);
        }
    }

    private String scoreFormateado(String s) {
        return (s == null) ? "" : s.trim().toUpperCase();
    }
}
