package com.johnson.practica.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "proyectos")
public class Proyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String numeroParte;
    private String cliente;
    private LocalDate fechaInicio;
    
    // AGREGA ESTO PARA QUE DEJE DE DAR EL ERROR 500
    private String liderProyecto; 

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ElementoChecklist> checklist = new ArrayList<>();
}