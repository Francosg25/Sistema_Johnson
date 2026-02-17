package com.johnson.practica.dto;

public class ReporteProgreso {
    private String nombreProyecto;
    private int totalEntregables;
    private int completados;
    private double porcentaje;

    public ReporteProgreso(String nombreProyecto, int totalEntregables, int completados, double porcentaje) {
        this.nombreProyecto = nombreProyecto;
        this.totalEntregables = totalEntregables;
        this.completados = completados;
        this.porcentaje = porcentaje;
    }

    // Getters
    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public int getTotalEntregables() {
        return totalEntregables;
    }

    public int getCompletados() {
        return completados;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    // Setters (optional, but good practice if mutability is desired)
    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public void setTotalEntregables(int totalEntregables) {
        this.totalEntregables = totalEntregables;
    }

    public void setCompletados(int completados) {
        this.completados = completados;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }
}