package com.johnson.practica.estrategias;

import com.johnson.practica.modelo.ElementoChecklist;
import org.springframework.stereotype.Component;

@Component
public class ScoreEstrategia implements ChecklistCampoEstrategia {
    @Override
    public boolean aplicaPara(String nombreCampo) {
        return "score".equalsIgnoreCase(nombreCampo);
    }

    @Override
    public boolean actualizar(ElementoChecklist elemento, String valorNuevo) {
        if (!valorNuevo.equalsIgnoreCase(elemento.getScore())) {
            elemento.setScore(valorNuevo);
            return true;
        }
        return false;
    }
}