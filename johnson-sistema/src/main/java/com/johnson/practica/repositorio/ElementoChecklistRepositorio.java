package com.johnson.practica.repositorio;

import com.johnson.practica.modelo.ElementoChecklist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElementoChecklistRepositorio extends JpaRepository<ElementoChecklist, Long> {

    @EntityGraph(attributePaths = {"proyecto", "catalogo"})
    List<ElementoChecklist> findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(Long proyectoId, String fase);

    @EntityGraph(attributePaths = {"proyecto", "catalogo"})
    List<ElementoChecklist> findByProyecto_Id(Long proyectoId);
    
    List<ElementoChecklist> findByControlEntregableIgnoreCase(String control);
}