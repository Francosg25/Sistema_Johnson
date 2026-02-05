package com.johnson.practica.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "catalogo_elementos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoElemento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo; // Ej: "PPAP-01"

    @Column(nullable = false)
    private String nombre;

    private String tipo;

    // Fase del proceso, por ejemplo "4. PPAP"
    private String fase;

    // Indica si el elemento es obligatorio en el checklist
    private boolean requerido = false;

    private String descripcion;
}