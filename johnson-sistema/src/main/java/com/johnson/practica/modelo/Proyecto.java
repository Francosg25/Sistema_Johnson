package com.johnson.practica.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "proyectos")
@Getter
@Setter
public class Proyecto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String numeroParte;
    private String cliente;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;
    
    private String liderProyecto; 

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate sop;

    private String projectEngineer;
    private String qualityEngineer;
    private String processEngineer;
    private String projectLeader;
    
    private String operationsManager;
    private String qualityManager;
    private String materialsManager;
    private String scsManager;
    private String financeManager;
    private String hrManager;

  
    private String aplicacion;
    private String linea;
    private String producto;
    private String razonRevision;
    private String programManager;

    // Nuevos campos para Master Timeline
    private String bu;
    private String planta;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaLineArrival;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPvBuild;
    
    @Column(columnDefinition = "TEXT")
    private String scope;

    private String launchEngineer;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPpap;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaTerminoSafeLaunch;

    private LocalDate fechaCar;
    private LocalDate fechaBuyoff;
    private LocalDate fechaTransit;
    private LocalDate fechaSop;

    @Column(name = "es_historico")
    private boolean esHistorico = false; 

    // ... (Mantén tus getters y setters manuales que ya tenías abajo)
    public boolean getEsHistorico() { return esHistorico; }
    public void setEsHistorico(boolean esHistorico) { this.esHistorico = esHistorico; }

    public LocalDate getFechaCar() { return fechaCar; }
    public void setFechaCar(LocalDate fechaCar) { this.fechaCar = fechaCar; }

    public LocalDate getFechaBuyoff() { return fechaBuyoff; }
    public void setFechaBuyoff(LocalDate fechaBuyoff) { this.fechaBuyoff = fechaBuyoff; }

    public LocalDate getFechaTransit() { return fechaTransit; }
    public void setFechaTransit(LocalDate fechaTransit) { this.fechaTransit = fechaTransit; }

    public LocalDate getFechaSop() { return fechaSop; }
    public void setFechaSop(LocalDate fechaSop) { this.fechaSop = fechaSop; }

    public String getAplicacion() {return aplicacion; }
    public void setAplicacion (String aplicacion) {this.aplicacion = aplicacion;}

    public String getLinea() {return linea; }
    public void setLinea (String linea) {this.linea = linea;}

     public String getProducto() {return producto; }
    public void setProducto (String producto) {this.producto = producto;}
    
     public String getRazonRevision() {return razonRevision; }
    public void setRazonRevision (String razonRevision) {this.razonRevision = razonRevision;}


    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ElementoChecklist> checklist = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<HitoProyecto> hitos = new ArrayList<>();

    private String faseActual = "APQP Program";
    private boolean archivado = false;
}