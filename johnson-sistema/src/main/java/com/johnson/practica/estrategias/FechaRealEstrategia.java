package com.johnson.practica.estrategias;

import com.johnson.practica.modelo.ElementoChecklist;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class FechaRealEstrategia implements ChecklistCampoEstrategia {

    @Override
    public boolean aplicaPara(String nombreCampo) {
        return "fechaReal".equals(nombreCampo); 
    }

    @Override
    public boolean actualizar(ElementoChecklist elemento, String valorNuevo) {
        try { 
            if (!valorNuevo.isEmpty()) {
                LocalDate nuevaFecha = LocalDate.parse(valorNuevo);
                if (elemento.getFechaReal() == null || !nuevaFecha.equals(elemento.getFechaReal())) {
                    elemento.setFechaReal(nuevaFecha); 
                    return true;
                }
            } else if (elemento.getFechaReal() != null) {
                elemento.setFechaReal(null); 
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}