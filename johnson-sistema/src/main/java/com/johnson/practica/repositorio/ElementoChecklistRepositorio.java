package com.johnson.practica.repositorio;

import com.johnson.practica.model.ElementoChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ElementoChecklistRepositorio extends JpaRepository<ElementoChecklist, Long> {
    
    // Método para buscar por Proyecto
    List<ElementoChecklist> findByProyectoId(Long proyectoId);

    // Spring Data interpreta: "Busca por ProyectoId" Y "entra a Catalogo" Y "busca Fase" que empiece con...
    List<ElementoChecklist> findByProyectoIdAndCatalogoFaseStartingWithOrderByCodigoAsc(Long proyectoId, String fasePrefix);

    // Método para contar cuántos elementos tiene un proyecto (Usado para saber si es nuevo o no)
    long countByProyectoId(Long proyectoId);
}