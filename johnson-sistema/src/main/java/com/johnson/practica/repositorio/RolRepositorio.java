package com.johnson.practica.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.johnson.practica.modelo.Rol;

import java.util.Optional;

public interface RolRepositorio extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
