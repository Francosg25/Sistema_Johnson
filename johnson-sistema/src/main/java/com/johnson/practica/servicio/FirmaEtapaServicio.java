package com.johnson.practica.servicio;

import com.johnson.practica.modelo.FirmaEtapa;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.FirmaEtapaRepositorio;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FirmaEtapaServicio {

    private final FirmaEtapaRepositorio firmaEtapaRepositorio;
    private final ProyectoRepositorio proyectoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public Map<String, FirmaEtapa> obtenerFirmasPorEtapa(Long proyectoId, Integer etapa) {
        return firmaEtapaRepositorio.findByProyectoIdAndEtapa(proyectoId, etapa)
                .stream()
                .collect(Collectors.toMap(FirmaEtapa::getRol, f -> f));
    }

    @Transactional
    public void firmar(Long proyectoId, Integer etapa, String rol, String username) {
        Proyecto proyecto = proyectoRepositorio.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        Usuario usuario = usuarioRepositorio.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si ya está firmado
        List<FirmaEtapa> existentes = firmaEtapaRepositorio.findByProyectoIdAndEtapa(proyectoId, etapa);
        boolean yaFirmado = existentes.stream().anyMatch(f -> f.getRol().equals(rol));
        
        if (yaFirmado) {
            throw new RuntimeException("Este rol ya ha firmado esta etapa");
        }

        FirmaEtapa firma = new FirmaEtapa(proyecto, etapa, rol, username, usuario.getNombreCompleto());
        firmaEtapaRepositorio.save(firma);

        bitacoraServicio.registrarAccion(username, "FIRMA_ELECTRONICA", 
                "Firmó como " + rol + " en el Stage " + etapa + " del proyecto " + proyecto.getNombre());
    }
}
