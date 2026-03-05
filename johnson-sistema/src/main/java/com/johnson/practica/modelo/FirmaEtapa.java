package com.johnson.practica.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "firmas_etapa")
@Getter
@Setter
@NoArgsConstructor
public class FirmaEtapa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    private Integer etapa; // 3, 4 o 5
    private String rol;    // Project Engineer, Quality Manager, etc.
    private String username;
    private String nombreCompleto;
    private LocalDateTime fechaFirma;

    public FirmaEtapa(Proyecto proyecto, Integer etapa, String rol, String username, String nombreCompleto) {
        this.proyecto = proyecto;
        this.etapa = etapa;
        this.rol = rol;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.fechaFirma = LocalDateTime.now();
    }
}
