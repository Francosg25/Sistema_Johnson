package com.johnson.practica.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.johnson.practica.modelo.Proyecto;

@Repository
public interface ProyectoRepositorio extends JpaRepository<Proyecto, Long> {
    // Aquí podrías agregar búsquedas personalizadas, por ejemplo:
    // List<Proyecto> findByCliente(String cliente);
}