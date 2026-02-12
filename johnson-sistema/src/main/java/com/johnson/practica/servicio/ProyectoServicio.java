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
        boolean isNew = proyecto.getId() == null;

        // Si es un proyecto existente, simplemente lo guardamos y retornamos.
        if (!isNew) {
            return proyectoRepositorio.save(proyecto);
        }

        // Si es un proyecto nuevo, primero lo guardamos para obtener un ID.
        Proyecto proyectoGuardado = proyectoRepositorio.save(proyecto);

        List<CatalogoElemento> plantillaCompleta = catalogoRepositorio.findAll();
        System.out.println("DEBUG: Creando checklist para nuevo proyecto. Plantillas encontradas: " + plantillaCompleta.size());

        if (plantillaCompleta.isEmpty()) {
            return proyectoGuardado; // No hay plantillas, no se puede crear checklist.
        }

        List<ElementoChecklist> nuevosItems = new java.util.ArrayList<>();
        for (CatalogoElemento molde : plantillaCompleta) {
            ElementoChecklist item = new ElementoChecklist();
            
            item.setProyecto(proyectoGuardado);
            item.setCatalogo(molde);
            
            // Copiamos los datos de la plantilla al nuevo item
            item.setCodigo(molde.getCodigo());
            item.setNombre(molde.getNombre());
            item.setGrupo(molde.getGrupo());
            item.setFase(molde.getFase());
            item.setTipoInput(molde.getTipoInput());
            item.setChampion(molde.getChampion());
            item.setEtapaVisual(molde.getEtapaVisual());
            
            // Inicializamos valores por defecto
            item.setEstado("PENDING"); 
            item.setScore("");
            item.setControlEntregable("Open");

            nuevosItems.add(item);
        }
        
        // Guardamos todos los nuevos items del checklist en una sola operación
        elementoRepositorio.saveAll(nuevosItems);
        
        return proyectoGuardado;
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
        List<ElementoChecklist> items = elementoRepositorio.findByProyectoIdAndCatalogoFaseStartingWithOrderByCodigoAsc(id, ""); 
        elementoRepositorio.deleteAll(items);
        
        // Luego borramos el proyecto
        proyectoRepositorio.deleteById(id);
    }
}