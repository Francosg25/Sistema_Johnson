package com.johnson.practica.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "contrasena", nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @Column(name = "password_changed")
    private boolean passwordChanged = false;

    @Column(name = "habilitado")
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles;
}
