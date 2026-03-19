package com.johnson.practica.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String mensaje;
    private String tipo; 
    private boolean leida;
    private LocalDateTime fechaCreacion;
    private String link;

    private String autorAccion; 
    private String autorDepartamento; 

    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario destinatario;
    private String usuarioDestino;

    public String setUsuarioDestino(String usuarioDestino) {
        this.usuarioDestino = usuarioDestino;
        return usuarioDestino;
    }

    public String getUsuarioDestino() {
        return usuarioDestino;
    }


    public Notificacion(String titulo, String mensaje, String tipo, Usuario destinatario, String autorAccion) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.destinatario = destinatario;
        this.autorAccion = autorAccion;
        this.leida = false;
        this.fechaCreacion = LocalDateTime.now();
    }
}