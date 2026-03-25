package com.johnson.practica.servicio;

import com.johnson.practica.modelo.*;
import com.johnson.practica.repositorio.*;

import com.johnson.practica.eventos.ProyectoCreadoEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProyectoServicio {

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CatalogoElementoRepositorio catalogoRepositorio;

    // Solo declaramos el repositorio UNA vez (borré la duplicada 'checklistRepositorio')
    @Autowired
    private ElementoChecklistRepositorio elementoRepositorio;

    @Autowired
    private com.johnson.practica.servicio.BitacoraServicio bitacoraServicio;

    @Transactional
    public Proyecto guardarProyecto(Proyecto proyecto) {
        boolean isNew = proyecto.getId() == null;

        if (!isNew) {
            return proyectoRepositorio.save(proyecto);
        }

        Proyecto proyectoGuardado = proyectoRepositorio.save(proyecto);

        // REGISTRAR EN BITÁCORA
        String usuario = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        bitacoraServicio.registrarAccion(usuario, "CREATE PROJECT", "Project created: " + proyectoGuardado.getNombre());

        List<CatalogoElemento> plantillaCompleta = catalogoRepositorio.findAll();
        System.out.println("DEBUG: Creando checklist para nuevo proyecto. Plantillas encontradas: " + plantillaCompleta.size());

        if (plantillaCompleta.isEmpty()) {
            return proyectoGuardado; 
        }

        List<ElementoChecklist> nuevosItems = new java.util.ArrayList<>();
        for (CatalogoElemento molde : plantillaCompleta) {
            ElementoChecklist item = new ElementoChecklist();
            
            item.setProyecto(proyectoGuardado);
            item.setCatalogo(molde);
            
            item.setCodigo(molde.getCodigo());
            item.setNombre(molde.getNombre());
            item.setGrupo(molde.getGrupo());
            item.setFase(molde.getFase());
            item.setTipoInput(molde.getTipoInput());
            item.setChampion(molde.getChampion());
            item.setEtapaVisual(molde.getEtapaVisual());
            
            item.setEstado("PENDING"); 
            item.setScore("");
            item.setControlEntregable("Open");

            nuevosItems.add(item);
        }
        
        elementoRepositorio.saveAll(nuevosItems);
        
        // PUBLICAR EVENTO PARA NOTIFICACIONES (Internas y Email)
        eventPublisher.publishEvent(new ProyectoCreadoEvent(proyectoGuardado, usuario));
        
        return proyectoGuardado;
    }

    public Proyecto buscarPorId(Long id) {
        return proyectoRepositorio.findById(id).orElse(null);
    }

    public List<Proyecto> obtenerTodos() {
        return proyectoRepositorio.findAll();
    }

    @Autowired
    private FirmaEtapaRepositorio firmaEtapaRepositorio;

    @Autowired
    private AdjuntoRepositorio adjuntoRepositorio;

    @Autowired
    private HitoProyectoRepositorio hitoProyectoRepositorio;

    @Transactional
    public void eliminarProyecto(Long id) {
        Proyecto p = buscarPorId(id);
        if (p != null) {
            String usuario = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            bitacoraServicio.registrarAccion(usuario, "DELETE PROJECT", "Project deleted: " + p.getNombre());
        }

        // Borramos firmas de etapa
        firmaEtapaRepositorio.deleteByProyecto_Id(id);
        
        // Borramos adjuntos del proyecto
        adjuntoRepositorio.deleteByProyecto_Id(id);

        // Borramos hitos del proyecto
        hitoProyectoRepositorio.deleteByProyecto_Id(id);

        // Borramos los items del checklist
        List<ElementoChecklist> items = elementoRepositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(id, ""); 
        elementoRepositorio.deleteAll(items);
        
        // Borramos el proyecto
        proyectoRepositorio.deleteById(id);
    }
}