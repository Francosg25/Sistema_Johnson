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
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String tipo;

    private String descripcion;
}
