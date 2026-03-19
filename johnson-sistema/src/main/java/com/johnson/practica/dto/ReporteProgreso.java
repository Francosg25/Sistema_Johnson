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
    private String fechaCar;
    private String fechaBuyoff;
    private String fechaTransit;
    private int onTimeCount;
    private int lateCount;
    private int needsActionCount;
    private int decisionCount;

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

    public String getFechaCar() { return fechaCar; }
    public void setFechaCar(String fechaCar) { this.fechaCar = fechaCar; }

    public String getFechaBuyoff() { return fechaBuyoff; }
    public void setFechaBuyoff(String fechaBuyoff) { this.fechaBuyoff = fechaBuyoff; }

    public String getFechaTransit() { return fechaTransit; }
    public void setFechaTransit(String fechaTransit) { this.fechaTransit = fechaTransit; }

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

    public int getOnTimeCount() { return onTimeCount; }
    public void setOnTimeCount(int onTimeCount) { this.onTimeCount = onTimeCount; }

    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }

    public int getNeedsActionCount() { return needsActionCount; }
    public void setNeedsActionCount(int needsActionCount) { this.needsActionCount = needsActionCount; }

    public int getDecisionCount() { return decisionCount; }
    public void setDecisionCount(int decisionCount) { this.decisionCount = decisionCount; }
}