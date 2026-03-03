package com.johnson.practica.repositorio;

import com.johnson.practica.modelo.Notificacion;
import com.johnson.practica.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepositorio extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByDestinatarioAndLeidaOrderByFechaCreacionDesc(Usuario destinatario, boolean leida);
    long countByDestinatarioAndLeida(Usuario destinatario, boolean leida);
}