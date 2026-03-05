package com.johnson.practica.repositorio;

import com.johnson.practica.modelo.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraRepositorio extends JpaRepository<Bitacora, Long> {
    List<Bitacora> findTop50ByOrderByFechaDesc();
}
