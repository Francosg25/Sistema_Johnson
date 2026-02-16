package com.johnson.practica.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.johnson.practica.modelo.CatalogoElemento;

@Repository
public interface CatalogoElementoRepositorio extends JpaRepository<CatalogoElemento, Long> {
    
}