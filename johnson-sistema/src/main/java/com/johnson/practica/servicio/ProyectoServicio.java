package com.johnson.practica.servicio;

import com.johnson.practica.model.*;
import com.johnson.practica.repositorio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProyectoServicio {

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Autowired
    private CatalogoElementoRepositorio catalogoRepositorio;

    @Autowired
    private ElementoChecklistRepositorio checklistRepositorio;

    // Método principal para guardar proyecto y generar sus 19 puntos
    @Transactional
    public Proyecto guardarProyecto(Proyecto proyecto) {
        // 1. Guardamos la portada del proyecto: si es nuevo fijamos la fecha de creación
        if (proyecto.getId() == null) {
            proyecto.setCreadoEn(LocalDateTime.now());
        }

        Proyecto proyectoGuardado = proyectoRepositorio.save(proyecto);

        // 2. Si es nuevo, generamos el checklist automático (si aún no existe)
        if (checklistRepositorio.findByProyectoId(proyectoGuardado.getId()).isEmpty()) {
            generarChecklistInicial(proyectoGuardado);
        }

        return proyectoGuardado;
    }

    private void generarChecklistInicial(Proyecto proyecto) {
        List<CatalogoElemento> catalogo = catalogoRepositorio.findAll();
        List<ElementoChecklist> nuevosElementos = new ArrayList<>();

        for (CatalogoElemento itemBase : catalogo) {
            ElementoChecklist elemento = ElementoChecklist.builder()
                    .proyecto(proyecto)
                    .titulo(itemBase.getNombre()) // Copiamos del catálogo
                    .fase(itemBase.getFase())     // Copiamos "4. PPAP"
                    .estado("PENDIENTE")          // Estado inicial
                    .build();
            
            nuevosElementos.add(elemento);
        }
        
        checklistRepositorio.saveAll(nuevosElementos);
    }
    
    public List<Proyecto> obtenerTodos() {
        return proyectoRepositorio.findAll();
    }

    public Proyecto buscarPorId(Long id) {
        return proyectoRepositorio.findById(id).orElse(null);
    }
}