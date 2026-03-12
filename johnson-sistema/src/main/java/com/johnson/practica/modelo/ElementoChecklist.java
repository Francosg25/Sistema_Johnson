package com.johnson.practica.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "elemento_checklist")
@Getter
@Setter

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

    private String codigo;
    private String nombre;
    private String grupo;
    private String fase;
    private String tipoInput;
    private String champion;
    private String etapaVisual;

    private String estado;     
    private String comentario;
    private String score;       
    private String controlEntregable; 
    
    private LocalDate fechaPlan;
    private LocalDate fechaReal;

    private boolean esMainEvent = false;


    @OneToMany(mappedBy = "elementoChecklist")
    private List<Adjunto> adjuntos;

}