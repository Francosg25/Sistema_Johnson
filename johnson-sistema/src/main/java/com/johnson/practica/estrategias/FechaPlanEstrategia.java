package com.johnson.practica.estrategias;

import com.johnson.practica.modelo.ElementoChecklist;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class FechaPlanEstrategia implements ChecklistCampoEstrategia {
    @Override
    public boolean aplicaPara(String nombreCampo) {
        return "fechaPlan".equalsIgnoreCase(nombreCampo);
    }

    @Override
    public boolean actualizar(ElementoChecklist elemento, String valorNuevo) {
        if (valorNuevo == null || valorNuevo.trim().isEmpty()) return false;
        try {
            LocalDate nueva = LocalDate.parse(valorNuevo.trim());
            if (elemento.getFechaPlan() == null || !elemento.getFechaPlan().equals(nueva)) {
                elemento.setFechaPlan(nueva);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}