package com.johnson.practica.repositorio;

import com.johnson.practica.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProyectoRepositorio extends JpaRepository<Proyecto, Long> {
    // Aquí podrías agregar búsquedas personalizadas, por ejemplo:
    // List<Proyecto> findByCliente(String cliente);
}