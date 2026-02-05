package com.johnson.practica.repositorio;

import com.johnson.practica.model.ElementoChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ElementoChecklistRepositorio extends JpaRepository<ElementoChecklist, Long> {
    List<ElementoChecklist> findByProyectoId(Long proyectoId);
}
