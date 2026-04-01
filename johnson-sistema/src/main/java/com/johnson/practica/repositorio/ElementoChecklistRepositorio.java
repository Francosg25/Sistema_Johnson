package com.johnson.practica.repositorio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    List<ElementoChecklist>findByProyecto(Proyecto proyecto);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT e.champion FROM ElementoChecklist e WHERE e.champion IS NOT NULL")
    List<String> findDistinctChampions();

    @EntityGraph(attributePaths = {"proyecto"})
    List<ElementoChecklist> findByScoreNotIgnoreCase(String score);

    // Tareas retrasadas 
    @Query("SELECT e FROM ElementoChecklist e WHERE e.proyecto = :proyecto AND e.fechaPlan < CURRENT_DATE AND (e.score IS NULL OR e.score != 'OK')")
    List<ElementoChecklist> buscarRetrasadasPorProyecto(@Param("proyecto") Proyecto proyecto);

    // Tareas próximas a vencer en los siguientes 30 días
    @Query("SELECT e FROM ElementoChecklist e WHERE e.proyecto = :proyecto AND e.fechaPlan >= CURRENT_DATE AND e.fechaPlan <= :fechaLimite AND (e.score IS NULL OR e.score != 'OK')")
    List<ElementoChecklist> buscarProximasPorProyecto(@Param("proyecto") Proyecto proyecto, @Param("fechaLimite") LocalDate fechaLimite);
    
}