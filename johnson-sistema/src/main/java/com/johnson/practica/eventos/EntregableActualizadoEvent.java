package com.johnson.practica.eventos;

import com.johnson.practica.modelo.ElementoChecklist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EntregableActualizadoEvent {
    private final ElementoChecklist elemento;
    private final String usuarioAfectado;
    private final String nombreCampo; 
    private final String valorNuevo;
    private final String autor;


    // Si el error persiste, añade esto manualmente para forzar al IDE:
    public String getAutor() {
        return autor;
    }

}