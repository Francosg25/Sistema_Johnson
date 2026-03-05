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
    
    // Management Site Management
    private String operationsManager;
    private String qualityManager;
    private String materialsManager;
    private String scsManager;
    private String financeManager;
    private String hrManager;

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ElementoChecklist> checklist = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<HitoProyecto> hitos = new ArrayList<>();
}