package com.johnson.practica.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.johnson.practica.modelo.ElementoChecklist;

import java.util.List;

@Repository
public interface ElementoChecklistRepositorio extends JpaRepository<ElementoChecklist, Long> {
    
    // Método para buscar por Proyecto
    List<ElementoChecklist> findByProyectoId(Long proyectoId);

    // Métodos para buscar elementos por Proyecto y Fase, optimizados para la nueva estructura
    List<ElementoChecklist> findByProyecto_IdAndFaseStartingWith(Long proyectoId, String fasePrefix);
    List<ElementoChecklist> findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(Long proyectoId, String fasePrefix);

    // Método para contar cuántos elementos tiene un proyecto (Usado para saber si es nuevo o no)
    long countByProyectoId(Long proyectoId);
}