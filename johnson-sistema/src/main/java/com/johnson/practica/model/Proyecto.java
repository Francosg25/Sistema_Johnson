package com.johnson.practica.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proyectos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String numeroParte;

    private String cliente;

    private LocalDate fechaSOP; // Fecha de Start of Production

    private String estado; // e.g. ON_TRACK, NEEDS_ACTION, COMPLETED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    private LocalDateTime creadoEn = LocalDateTime.now();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ElementoChecklist> elementosChecklist = new ArrayList<>();
}
