package com.johnson.practica.estrategias;

import com.johnson.practica.modelo.ElementoChecklist;
import org.springframework.stereotype.Component;

@Component
public class ControlEstrategia implements ChecklistCampoEstrategia {

    @Override
    public boolean aplicaPara(String nombreCampo) {
        return "controlEntregable".equals(nombreCampo);
    }

    @Override
    public boolean actualizar(ElementoChecklist elemento, String valorNuevo) {
        String valorActual = elemento.getControlEntregable();
        
        if (valorNuevo != null && !valorNuevo.equals(valorActual)) {
            elemento.setControlEntregable(valorNuevo); 
            return true; 
        }
        
        return false; 
    }
}