package com.johnson.practica.repositorio;

import com.johnson.practica.modelo.HitoProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HitoProyectoRepositorio extends JpaRepository<HitoProyecto, Long> {
    List<HitoProyecto> findByProyecto_Id(Long proyectoId);
}
