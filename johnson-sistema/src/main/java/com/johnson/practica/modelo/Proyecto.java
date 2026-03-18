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

    // Roles para Firmas APQP
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

    private LocalDate fechaCar;
    private LocalDate fechaBuyoff;
    private LocalDate fechaTransit;
    private LocalDate fechaSop;

    public LocalDate getFechaCar() { return fechaCar; }
    public void setFechaCar(LocalDate fechaCar) { this.fechaCar = fechaCar; }

    public LocalDate getFechaBuyoff() { return fechaBuyoff; }
    public void setFechaBuyoff(LocalDate fechaBuyoff) { this.fechaBuyoff = fechaBuyoff; }

    public LocalDate getFechaTransit() { return fechaTransit; }
    public void setFechaTransit(LocalDate fechaTransit) { this.fechaTransit = fechaTransit; }

    public LocalDate getFechaSop() { return fechaSop; }
    public void setFechaSop(LocalDate fechaSop) { this.fechaSop = fechaSop; }



    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ElementoChecklist> checklist = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<HitoProyecto> hitos = new ArrayList<>();

    private String faseActual = "APQP Program";
    private boolean archivado = false;
}