package com.johnson.practica.servicio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeeklyDigestServicio {

    @Autowired
    private ElementoChecklistRepositorio checklistRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private EmailServicio emailServicio;

    @Autowired
    private ChecklistServicio checklistServicio;

    // (Cambia a "0 * * * * ?" para probarlo cada minuto)
    @Scheduled(cron = "0 0 7 * * MON")
    @Transactional(readOnly = true)
    public void generarYEnviarResumenSemanal() {
        LocalDate hoy = LocalDate.now();
        LocalDate proximoDomingo = hoy.plusDays(7); 

        List<ElementoChecklist> tareasCriticas = checklistRepositorio.findAll().stream()
            .filter(t -> t.getFechaPlan() != null)
            .filter(t -> !t.getFechaPlan().isAfter(proximoDomingo))
            .filter(t -> !t.getProyecto().getEsHistorico())
            .filter(t -> !"OK".equalsIgnoreCase(t.getScore()) && !"N/A".equalsIgnoreCase(t.getScore()))
            .collect(Collectors.toList());

        if (tareasCriticas.isEmpty()) return; 

        Map<String, List<ElementoChecklist>> tareasPorDepartamento = tareasCriticas.stream()
            .filter(t -> t.getChampion() != null)
            .collect(Collectors.groupingBy(t -> checklistServicio.normalizarChampion(t.getChampion()).toUpperCase()));

        List<Usuario> todosUsuarios = usuarioRepositorio.findAll();

        for (Usuario usuario : todosUsuarios) {
            if (usuario.isEnabled() && usuario.getCorreo() != null && usuario.getDepartamento() != null) {
                String deptoUsuario = usuario.getDepartamento().toUpperCase();
                
                if (tareasPorDepartamento.containsKey(deptoUsuario)) {
                    List<ElementoChecklist> tareasDelUsuario = tareasPorDepartamento.get(deptoUsuario);
                    emailServicio.enviarWeeklyDigest(usuario, tareasDelUsuario);
                }
            }
        }
    }
}