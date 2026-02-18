package com.johnson.practica.repositorio;

import com.johnson.practica.modelo.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjuntoRepositorio extends JpaRepository<Adjunto, Long> {
    // Aquí puedes agregar métodos personalizados si es necesario
    
}