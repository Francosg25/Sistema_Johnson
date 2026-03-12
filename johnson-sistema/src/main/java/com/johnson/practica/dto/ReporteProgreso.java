package com.johnson.practica.dto;

public class ReporteProgreso {
    private Long id;
    private String nombreProyecto;
    private String cliente;
    private String numeroParte;
    private String liderProyecto;
    private String sop;
    private int totalEntregables;
    private int completados;
    private double porcentaje;
    private Double riesgo; 
    private java.time.LocalDate carDate;
    private java.time.LocalDate buyoffDate;
    private java.time.LocalDate transitDate;


    public ReporteProgreso(Long id, String nombreProyecto, String cliente, String numeroParte, String liderProyecto, String sop, int totalEntregables, int completados, double porcentaje) {
        this.id = id;
        this.nombreProyecto = nombreProyecto;
        this.cliente = cliente;
        this.numeroParte = numeroParte;
        this.liderProyecto = liderProyecto;
        this.sop = sop;
        this.totalEntregables = totalEntregables;
        this.completados = completados;
        this.porcentaje = porcentaje;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreProyecto() { return nombreProyecto; }
    public void setNombreProyecto(String nombreProyecto) { this.nombreProyecto = nombreProyecto; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getNumeroParte() { return numeroParte; }
    public void setNumeroParte(String numeroParte) { this.numeroParte = numeroParte; }

    public String getLiderProyecto() { return liderProyecto; }
    public void setLiderProyecto(String liderProyecto) { this.liderProyecto = liderProyecto; }

    public String getSop() { return sop; }
    public void setSop(String sop) { this.sop = sop; }

    public int getTotalEntregables() { return totalEntregables; }
    public void setTotalEntregables(int totalEntregables) { this.totalEntregables = totalEntregables; }

    public int getCompletados() { return completados; }
    public void setCompletados(int completados) { this.completados = completados; }

    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }

    public Double getRiesgo() { return riesgo; }
    public void setRiesgo(Double riesgo) { this.riesgo = riesgo; }

    public java.time.LocalDate getCarDate() { return carDate; }
    public void setCarDate(java.time.LocalDate carDate) { this.carDate = carDate; }

    public java.time.LocalDate getBuyoffDate() { return buyoffDate; }
    public void setBuyoffDate(java.time.LocalDate buyoffDate) { this.buyoffDate = buyoffDate; }

    public java.time.LocalDate getTransitDate() { return transitDate; }
    public void setTransitDate(java.time.LocalDate transitDate) { this.transitDate = transitDate; }

}