package com.johnson.practica.dto;

import java.util.List;

public class ReporteCascada {
    
    private String nombreProyecto;
    private List<Double> porcentajesPorFase;

    public ReporteCascada(String nombreProyecto, List<Double> porcentajesPorFase) {
        this.nombreProyecto = nombreProyecto;
        this.porcentajesPorFase = porcentajesPorFase;
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
}