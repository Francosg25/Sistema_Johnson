package com.johnson.practica.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adjuntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adjunto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;

    private String tipoContenido;

    @Column(name = "datos", nullable = false, columnDefinition = "BYTEA")
    @Basic(fetch = FetchType.LAZY)
    private byte[] datos;

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