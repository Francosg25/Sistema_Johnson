package com.johnson.practica.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReporteCascada {
    
    private String nombreProyecto;
    private List<Double> porcentajesPorFase;
    private String sop;

    public ReporteCascada(String nombreProyecto, List<Double> porcentajesPorFase) {
        this.nombreProyecto = nombreProyecto;
        this.porcentajesPorFase = porcentajesPorFase;
        this.sop = (sop != null) ? sop.toString() : "Sin SOP";
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public List<Double> getPorcentajesPorFase() {
        return porcentajesPorFase;
    }

    public void setPorcentajesPorFase(List<Double> porcentajesPorFase) {
        this.porcentajesPorFase = porcentajesPorFase;
    }

    public String getSop() { return sop; }

    
}