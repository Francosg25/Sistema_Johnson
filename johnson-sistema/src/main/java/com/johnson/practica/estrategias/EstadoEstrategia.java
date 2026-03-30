package com.johnson.practica.estrategias;

import com.johnson.practica.modelo.ElementoChecklist;
import org.springframework.stereotype.Component;

@Component
public class EstadoEstrategia implements ChecklistCampoEstrategia {
    @Override
    public boolean aplicaPara(String nombreCampo) {
        return "estado".equalsIgnoreCase(nombreCampo);
    }

    @Override
    public boolean actualizar(ElementoChecklist elemento, String valorNuevo) {
        if (valorNuevo == null) valorNuevo = "";
        String actual = (elemento.getEstado() == null) ? "" : elemento.getEstado();
        
        if (!valorNuevo.equalsIgnoreCase(actual)) {
            elemento.setEstado(valorNuevo);
            return true;
        }
        return false;
    }
}