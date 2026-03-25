package com.johnson.practica.eventos;

import com.johnson.practica.modelo.Proyecto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Evento que se dispara cuando un nuevo proyecto es guardado exitosamente.
 */
@Getter
@AllArgsConstructor
public class ProyectoCreadoEvent {
    private final Proyecto proyecto;
    private final String autor;
}
