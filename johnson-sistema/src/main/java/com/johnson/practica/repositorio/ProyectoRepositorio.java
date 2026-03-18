package com.johnson.practica.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
import com.johnson.practica.modelo.Proyecto;

@Repository
public interface ProyectoRepositorio extends JpaRepository<Proyecto, Long> {
    java.util.List<Proyecto> findAllByOrderByIdAsc();

    List<Proyecto> findByEsHistoricoFalse(); 

    List<Proyecto> findByEsHistoricoTrue();
}