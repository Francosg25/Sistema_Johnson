package com.johnson.practica.dto;

public class ReporteEstadoGlobal {
    private double onTimePercentage;
    private double latePercentage;
    private double needsActionPercentage;
    private double decisionPercentage;
    private int totalDeliverables; 

    public ReporteEstadoGlobal() {
        // Constructor por defecto
    }

    // Getters y Setters
    public double getOnTimePercentage() {
        return onTimePercentage;
    }

    public void setOnTimePercentage(double onTimePercentage) {
        this.onTimePercentage = onTimePercentage;
    }

    public double getLatePercentage() {
        return latePercentage;
    }

    public void setLatePercentage(double latePercentage) {
        this.latePercentage = latePercentage;
    }

    public double getNeedsActionPercentage() {
        return needsActionPercentage;
    }

    public void setNeedsActionPercentage(double needsActionPercentage) {
        this.needsActionPercentage = needsActionPercentage;
    }

    public double getDecisionPercentage() {
        return decisionPercentage;
    }

    public void setDecisionPercentage(double decisionPercentage) {
        this.decisionPercentage = decisionPercentage;
    }

    public int getTotalDeliverables() {
        return totalDeliverables;
    }

    public void setTotalDeliverables(int totalDeliverables) {
        this.totalDeliverables = totalDeliverables;
    }
}
