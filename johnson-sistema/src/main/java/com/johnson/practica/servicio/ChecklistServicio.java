package com.johnson.practica.servicio;

import com.johnson.practica.model.ElementoChecklist;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChecklistServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    // Obtiene solo los HITOS (Programa APQP)
    // CORRECCIÓN: Usamos 'CatalogoFase' porque la fase está dentro del Catálogo
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerHitosPrograma(Long proyectoId) {
        return repositorio.findByProyectoIdAndCatalogoFaseStartingWith(proyectoId, "0");
    }

    // Obtiene solo las PREGUNTAS de validación (Stage 2)
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage2(Long proyectoId) {
        return repositorio.findByProyectoIdAndCatalogoFaseStartingWith(proyectoId, "2");
    }

    // Lógica para guardar cambios (Usado por el AJAX)
    @Transactional
    public ElementoChecklist actualizarElemento(Long id, String estado, String comentario) {
        ElementoChecklist elemento = repositorio.findById(id).orElse(null);
        
        if (elemento != null) {
            if (estado != null) elemento.setEstado(estado);
            if (comentario != null) elemento.setComentario(comentario);
            return repositorio.save(elemento);
        }
        return null;
    }
}