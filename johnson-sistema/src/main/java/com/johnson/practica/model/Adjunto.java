package com.johnson.practica.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "adjuntos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adjunto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;

    private String tipoContenido;

    @Column(nullable = false)
    private String ruta; // filesystem path

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subido_por_id")
    private Usuario subidoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_checklist_id")
    private ElementoChecklist elementoChecklist;

    private LocalDateTime subidoEn = LocalDateTime.now();
}
