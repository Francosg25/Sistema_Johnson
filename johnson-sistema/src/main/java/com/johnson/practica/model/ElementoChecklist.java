package com.johnson.practica.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "elemento_checklist")
public class ElementoChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @ManyToOne
    @JoinColumn(name = "catalogo_id")
    private CatalogoElemento catalogo;

    // Datos descriptivos (Copiados del catálogo al crear)
    private String codigo;
    private String nombre;
    private String grupo;
    private String fase;
    private String tipoInput;
    private String champion;
    private String etapaVisual;

    // Datos de respuesta (Llenados por el usuario)
    private String estado;      // OK, NOK, NA
    private String comentario;
    private String score;       // Puntuación
    private String controlEntregable; // Closed on time, etc.
    
    // ESTOS SON LOS QUE FALTABAN Y CAUSABAN EL ERROR:
    private LocalDate fechaPlan;
    private LocalDate fechaReal;
}