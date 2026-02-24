package com.johnson.practica.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReporteEstadoGlobal {
    private double onTimePercentage;
    private double latePercentage;
    private double needsActionPercentage;
    private double decisionPercentage;
    
    private int totalDeliverables; 
    private int onTimeCount;
    private int delayedCount;
    private int fulfilledCount;
    private int escalationCount;

    private int riskHigh;
    private int riskLow;

    public ReporteEstadoGlobal() {
    }
}
