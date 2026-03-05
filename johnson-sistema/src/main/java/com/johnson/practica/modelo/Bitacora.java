package com.johnson.practica.modelo;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "bitacora_auditoria")
public class Bitacora {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuario; 

    @Column(nullable = false)
    private String accion; 

    @Column(length = 500)
    private String detalle; 

    @Column(nullable = false)
    private LocalDateTime fecha; 

    public Bitacora() {}

    public Bitacora(String usuario, String accion, String detalle) {
        this.usuario = usuario;
        this.accion = accion;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getAccion() { return accion; }
    public String getDetalle() { return detalle; }
    public LocalDateTime getFecha() { return fecha; }
    
    public String getFechaFormateada() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return fecha.format(formatter);
    }
}



