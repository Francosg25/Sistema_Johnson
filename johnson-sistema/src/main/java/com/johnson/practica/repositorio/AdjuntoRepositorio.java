package com.johnson.practica.repositorio;

import com.johnson.practica.modelo.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdjuntoRepositorio extends JpaRepository<Adjunto, Long> {
    
    @Query("SELECT a FROM Adjunto a LEFT JOIN FETCH a.proyecto LEFT JOIN FETCH a.elementoChecklist ORDER BY a.subidoEn DESC")
    List<Adjunto> findAllConDetalles();

    void deleteByProyecto_Id(Long proyectoId);
}