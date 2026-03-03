package com.johnson.practica.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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