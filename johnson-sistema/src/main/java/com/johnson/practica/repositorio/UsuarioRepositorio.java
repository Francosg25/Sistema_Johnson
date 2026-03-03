package com.johnson.practica.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.johnson.practica.modelo.Usuario;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);

    Optional<Usuario> findByCorreo(String correo);
}
