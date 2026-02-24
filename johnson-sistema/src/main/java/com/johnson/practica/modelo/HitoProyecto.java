package com.johnson.practica.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "hitos_proyecto")
@Getter
@Setter
public class HitoProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private LocalDate fecha;
    private String etapaAsociada; 
    private Integer porcentajeObjetivo; // Nuevo: Para definir hitos de progreso (ej: 50%)

    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    public HitoProyecto() {}

    public HitoProyecto(String nombre, LocalDate fecha, String etapaAsociada, Proyecto proyecto, Integer porcentajeObjetivo) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.etapaAsociada = etapaAsociada;
        this.proyecto = proyecto;
        this.porcentajeObjetivo = porcentajeObjetivo;
    }
}
