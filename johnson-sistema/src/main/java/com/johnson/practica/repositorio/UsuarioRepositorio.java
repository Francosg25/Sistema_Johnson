package com.johnson.practica.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.johnson.practica.modelo.Usuario;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
