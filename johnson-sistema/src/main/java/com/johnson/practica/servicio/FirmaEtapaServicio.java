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
    private final NotificacionServicio notificacionServicio;
    private final ChecklistServicio checklistServicio;

    private static final List<String> ROLES_REQUERIDOS = List.of(
        "Project Engineer", "Quality Engineer", "Process Engineer", "Project Leader",
        "Operations Manager", "Quality Manager", "Materials Manager", "SCS Manager", 
        "Finance Manager", "HR Manager"
    );

    public Map<String, FirmaEtapa> obtenerFirmasPorEtapa(Long proyectoId, Integer etapa) {
        return firmaEtapaRepositorio.findByProyectoIdAndEtapa(proyectoId, etapa)
                .stream()
                .collect(Collectors.toMap(FirmaEtapa::getRol, f -> f));
    }

    @Transactional
    public void eliminarFirma(Long proyectoId, Integer etapa, String rol) {
        firmaEtapaRepositorio.deleteByProyecto_IdAndEtapaAndRol(proyectoId, etapa, rol);
    }

    @Transactional
    public void firmar(Long proyectoId, Integer etapa, String rol, String username) {
        Proyecto proyecto = proyectoRepositorio.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // --- VALIDACIÓN DE SECUENCIA DE GATES ---
        
        // 1. Validar que el checklist del Gate actual esté completo antes de permitir firmas
        List<String> erroresPropioGate = checklistServicio.validarGate(proyectoId, etapa);
        if (!erroresPropioGate.isEmpty()) {
            throw new RuntimeException("❌ Checklist for Gate " + etapa + " is not complete: " + String.join(", ", erroresPropioGate));
        }

        // 2. Gate 3 requiere que Gate 2 esté completo
        if (etapa == 3) {
            List<String> erroresGate2 = checklistServicio.validarGate(proyectoId, 2);
            if (!erroresGate2.isEmpty()) {
                throw new RuntimeException("❌ Gate 2 is not complete: " + String.join(", ", erroresGate2));
            }
        } 
        // 3. Gate N requiere firmas completas de Gate N-1
        else if (etapa > 3) {
            int etapaAnterior = etapa - 1;
            List<FirmaEtapa> firmasAnteriores = firmaEtapaRepositorio.findByProyectoIdAndEtapa(proyectoId, etapaAnterior);
            
            for (String rolReq : ROLES_REQUERIDOS) {
                boolean firmado = firmasAnteriores.stream().anyMatch(f -> f.getRol().equalsIgnoreCase(rolReq));
                if (!firmado) {
                    throw new RuntimeException("❌ Gate " + etapaAnterior + " is not complete. All required roles must sign Gate " + etapaAnterior + " before signing Gate " + etapa + ".");
                }
            }
        }
        // ----------------------------------------

        Usuario usuario = usuarioRepositorio.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FirmaEtapa> existentes = firmaEtapaRepositorio.findByProyectoIdAndEtapa(proyectoId, etapa);
        boolean yaFirmado = existentes.stream().anyMatch(f -> f.getRol().equals(rol));
        
        if (yaFirmado) {
            throw new RuntimeException("This role has already signed this stage");
        }

        FirmaEtapa firma = new FirmaEtapa();
        firma.setProyecto(proyecto);
        firma.setEtapa(etapa);
        firma.setRol(rol);
        firma.setUsername(username);
        firma.setNombreCompleto(usuario.getNombreCompleto());
        firma.setFechaFirma(java.time.LocalDateTime.now());
        
        firmaEtapaRepositorio.save(firma);

        bitacoraServicio.registrarAccion(username, "ELECTRONIC_SIGNATURE", 
                "Signed as " + rol + " in Stage " + etapa + " of project " + proyecto.getNombre());
                
        String msj = "✅ APPROVAL SEAL applied by " + usuario.getNombreCompleto() + 
                     " as " + rol + " in Gate " + etapa + " (" + proyecto.getNombre() + ")";
        String url = "/proyectos/checklist/" + proyecto.getId();
        
        notificacionServicio.alertarATodos("Gate " + etapa + " Approval", msj, "SUCCESS", url, username);
    }
}
