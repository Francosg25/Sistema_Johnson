package com.johnson.practica.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "catalogo_elementos")
public class CatalogoElemento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;      
    private String nombre;      
    private String fase;        
    
    private String grupo;       
    private String tipoInput;   
    private boolean requerido;

    private String champion;    
    private String etapaVisual; 
}