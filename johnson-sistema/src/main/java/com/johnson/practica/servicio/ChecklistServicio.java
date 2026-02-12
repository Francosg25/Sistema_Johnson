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

    // 1. Obtiene solo los HITOS (Programa APQP - Fase 0)
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerHitosPrograma(Long proyectoId) {
        return repositorio.findByProyectoIdAndCatalogoFaseStartingWith(proyectoId, "0");
    }

    // 2. MÉTODO NUEVO: Sirve para Stage 2, 3, 4 y 5
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorFase(Long proyectoId, String prefijoFase) {
        return repositorio.findByProyectoIdAndCatalogoFaseStartingWith(proyectoId, prefijoFase);
    }

    // 3. Obtiene solo Stage 2 (Lo dejamos por compatibilidad con tu código anterior)
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage2(Long proyectoId) {
        return obtenerPorFase(proyectoId, "2");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage3(Long proyectoId) {
        return obtenerPorFase(proyectoId, "3");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage4(Long proyectoId) {        
        return obtenerPorFase(proyectoId, "4");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage5(Long proyectoId) {
        return obtenerPorFase(proyectoId, "5");
    }


    // 4. Guardar cambios
    @Transactional
    public ElementoChecklist actualizarElemento(Long id, String estado, String comentario, String controlEntregable, String score) {
        ElementoChecklist elemento = repositorio.findById(id).orElse(null);
        if (elemento != null) {
            if (estado != null) elemento.setEstado(estado);
            if (comentario != null) elemento.setComentario(comentario);
            if (controlEntregable != null) elemento.setControlEntregable(controlEntregable);
            if (score != null) elemento.setScore(score);
            return repositorio.save(elemento);
        }
        return null;
    }
}