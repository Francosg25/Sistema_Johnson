package com.johnson.practica.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.johnson.practica.modelo.ElementoChecklist;

import java.util.List;

@Repository
public interface ElementoChecklistRepositorio extends JpaRepository<ElementoChecklist, Long> {
    
    List<ElementoChecklist> findByProyectoId(Long proyectoId);

    List<ElementoChecklist> findByProyecto_IdAndFaseStartingWith(Long proyectoId, String fasePrefix);
    List<ElementoChecklist> findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(Long proyectoId, String fasePrefix);

    long countByProyectoId(Long proyectoId);

    List<ElementoChecklist> findByProyecto_Id(Long id);
}