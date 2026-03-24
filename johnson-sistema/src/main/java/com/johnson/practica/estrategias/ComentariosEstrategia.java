package com.johnson.practica.estrategias;

import com.johnson.practica.modelo.ElementoChecklist;
import org.springframework.stereotype.Component;

@Component
public class ComentariosEstrategia implements ChecklistCampoEstrategia {

    @Override
    public boolean aplicaPara(String nombreCampo) {
        return "comentarios".equalsIgnoreCase(nombreCampo) || 
               "comentario".equalsIgnoreCase(nombreCampo) || 
               "remarks".equalsIgnoreCase(nombreCampo) ||
               "remark".equalsIgnoreCase(nombreCampo);
    }

    @Override
    public boolean actualizar(ElementoChecklist elemento, String valorNuevo) {
        String valorActual = elemento.getComentario();
        if (valorActual == null) {
            valorActual = "";
        }
        
        if (!valorActual.equals(valorNuevo)) {
            elemento.setComentario(valorNuevo);
            return true; 
        }
        
        return false;
    }
}