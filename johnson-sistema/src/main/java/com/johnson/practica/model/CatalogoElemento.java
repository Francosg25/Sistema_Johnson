package com.johnson.practica.model;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_elementos")
public class CatalogoElemento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;      // Ej: "HITO-01", "S2-05"
    private String nombre;      // La pregunta o el nombre del hito
    private String fase;        // Ej: "0. Programa", "2. Stage 2"
    
    private String tipoInput;   
    private String grupo;       

    private boolean requerido;
    
    private String tipoFormato; 
    
    private String subSeccion; // Ej: "Información Preliminar", "Diseño", "SCS"

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }
    public String getTipoInput() { return tipoInput; }
    public void setTipoInput(String tipoInput) { this.tipoInput = tipoInput; }
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    public boolean isRequerido() { return requerido; }
    public void setRequerido(boolean requerido) { this.requerido = requerido; }
}