package com.johnson.practica.repositorio;

import com.johnson.practica.modelo.FirmaEtapa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FirmaEtapaRepositorio extends JpaRepository<FirmaEtapa, Long> {
    List<FirmaEtapa> findByProyectoIdAndEtapa(Long proyectoId, Integer etapa);
    List<FirmaEtapa> findByProyectoId(Long proyectoId);
    void deleteByProyecto_Id(Long proyectoId);
}
