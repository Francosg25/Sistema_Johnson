package com.johnson.practica.repositorio;

import com.johnson.practica.model.CatalogoElemento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogoElementoRepositorio extends JpaRepository<CatalogoElemento, Long> {
    // Aquí podrías agregar búsquedas personalizadas, por ejemplo:
    // List<CatalogoElemento> findByTipo(String tipo);
}