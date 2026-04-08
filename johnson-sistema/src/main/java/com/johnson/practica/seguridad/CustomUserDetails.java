package com.johnson.practica.seguridad;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private final String departamento;
    private final String nombreCompleto;

    public CustomUserDetails(String username, String password, boolean enabled, 
                             boolean accountNonExpired, boolean credentialsNonExpired, 
                             boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
                             String departamento, String nombreCompleto) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.departamento = departamento;
        this.nombreCompleto = nombreCompleto;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getNombreCompletoDepartamento() {
        if (departamento == null) return "Active User";
        
        switch (departamento.toUpperCase()) {
            case "PM": return "Program Manager";
            case "DE": return "Design Engineering";
            case "QE": return "Quality Engineering";
            case "PE": return "Process Engineering";
            case "PROJ": return "Project Management";
            case "SCS": return "Supply Chain";
            case "FIN": return "Finance";
            case "OPS": return "Operations";
            case "HR": return "Human Resources";
            case "MAT": return "Materials";
            case "ALL": return "General Management";
            case "MANAGEMENT": return "Management";
            default: return departamento;
        }
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }
}
