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


    @EntityGraph(attributePaths = {"catalogo", "adjuntos"})
    List<ElementoChecklist> findByProyecto_IdOrderByCodigoAsc(Long proyectoId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT e.champion FROM ElementoChecklist e WHERE e.champion IS NOT NULL")
    List<String> findDistinctChampions();

    @EntityGraph(attributePaths = {"proyecto"})
    List<ElementoChecklist> findByScoreNotIgnoreCase(String score);
}