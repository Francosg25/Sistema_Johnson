package com.johnson.practica.estrategias;

import com.johnson.practica.modelo.ElementoChecklist;

public interface ChecklistCampoEstrategia {
    boolean aplicaPara(String nombreCampo);
    
    boolean actualizar(ElementoChecklist elemento, String valorNuevo);
}