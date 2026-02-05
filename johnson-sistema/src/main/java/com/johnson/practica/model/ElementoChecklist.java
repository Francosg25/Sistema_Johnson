package com.johnson.practica.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "elementos_checklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElementoChecklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @Column(nullable = false)
    private String titulo;

    private String descripcion;

    private String estado;
     
    private String fase; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    private LocalDate fechaPlan;

    private LocalDate fechaReal;

    @OneToMany(mappedBy = "elementoChecklist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Adjunto> adjuntos = new ArrayList<>();
}
