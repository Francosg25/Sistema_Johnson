package com.johnson.practica.servicio;

import com.johnson.practica.model.*;
import com.johnson.practica.repositorio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProyectoServicio {

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Autowired
    private CatalogoElementoRepositorio catalogoRepositorio;

    // Solo declaramos el repositorio UNA vez (borré la duplicada 'checklistRepositorio')
    @Autowired
    private ElementoChecklistRepositorio elementoRepositorio;

    @Transactional
    public Proyecto guardarProyecto(Proyecto proyecto) {
        //Guardamos el proyecto (
        
        Proyecto nuevoProyecto = proyectoRepositorio.save(proyecto);

       
        long countChecklist = elementoRepositorio.countByProyectoId(nuevoProyecto.getId());
        
        if (countChecklist == 0) {
            List<CatalogoElemento> plantillaCompleta = catalogoRepositorio.findAll();
            System.out.println("DEBUG: Copiando " + plantillaCompleta.size() + " elementos al nuevo proyecto...");

            for (CatalogoElemento molde : plantillaCompleta) {
                ElementoChecklist item = new ElementoChecklist();
                
                // --- Vinculación ---
                item.setProyecto(nuevoProyecto);
                item.setCatalogo(molde);
                
                // --- Copia de Datos Estructurales (Vitales para Stages 2-5) ---
                item.setCodigo(molde.getCodigo());
                item.setNombre(molde.getNombre());
                item.setGrupo(molde.getGrupo());       // IMPORTANTE: Para subtítulos en Stage 2
                item.setFase(molde.getFase());         // IMPORTANTE: Para saber en qué pestaña va
                item.setTipoInput(molde.getTipoInput()); // IMPORTANTE: Para saber si es Fecha o Si/No
                
                item.setChampion(molde.getChampion());
                item.setEtapaVisual(molde.getEtapaVisual());
                
                // --- Inicialización de Valores por Defecto ---
                item.setEstado("PENDING"); 
                item.setScore(""); // Inicializamos vacío (String) como pediste
                item.setControlEntregable("Open"); 

                elementoRepositorio.save(item);
            }
            // Forzamos la escritura en DB para que estén listos inmediatamente
            elementoRepositorio.flush(); 
        }
        
        return nuevoProyecto;
    }

    public Proyecto buscarPorId(Long id) {
        return proyectoRepositorio.findById(id).orElse(null);
    }

    public List<Proyecto> obtenerTodos() {
        return proyectoRepositorio.findAll();
    }

    @Transactional
    public void eliminarProyecto(Long id) {
        // Primero borramos los items del checklist para evitar error de llave foránea
        // Usamos el método que ya tienes en tu repo o uno genérico de borrado
        List<ElementoChecklist> items = elementoRepositorio.findByProyectoIdAndCatalogoFaseStartingWith(id, ""); 
        elementoRepositorio.deleteAll(items);
        
        // Luego borramos el proyecto
        proyectoRepositorio.deleteById(id);
    }
}