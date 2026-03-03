package com.johnson.practica.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@Getter 
@Setter
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre; // e.g., ROLE_ADMIN, ROLE_CHAMPION, ROLE_VIEWER

    @ManyToMany(mappedBy = "roles")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Usuario> usuarios;
}
