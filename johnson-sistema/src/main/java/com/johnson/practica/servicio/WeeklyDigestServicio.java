package com.johnson.practica.servicio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.repositorio.ProyectoRepositorio;
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

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

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

    @Scheduled(cron = "0 0 8 * * MON") 
    @Transactional(readOnly = true)
    public void generarYEnviarAlertasPorProyecto() {
        System.out.println("⏳ [INICIO] Escaneo de proyectos APQP...");

        LocalDate hoy = LocalDate.now();
        LocalDate limiteSiguienteMes = hoy.plusDays(30);

        List<Proyecto> proyectosActivos = proyectoRepositorio.findByEsHistoricoFalse();
        List<Usuario> todosLosUsuarios = usuarioRepositorio.findAll();

        System.out.println("📊 Proyectos activos encontrados: " + proyectosActivos.size());
        System.out.println("👥 Total de usuarios en BD: " + todosLosUsuarios.size());

        for (Proyecto proyecto : proyectosActivos) {
            
            List<ElementoChecklist> tareasDelProyecto = checklistRepositorio.findByProyecto(proyecto).stream()
                .filter(t -> !"OK".equalsIgnoreCase(t.getScore()) && !"N/A".equalsIgnoreCase(t.getScore()))
                .filter(t -> t.getFechaPlan() != null)
                .collect(Collectors.toList());

            List<ElementoChecklist> retrasadas = tareasDelProyecto.stream()
                .filter(t -> t.getFechaPlan().isBefore(hoy))
                .collect(Collectors.toList());

            List<ElementoChecklist> proximas = tareasDelProyecto.stream()
                .filter(t -> !t.getFechaPlan().isBefore(hoy) && !t.getFechaPlan().isAfter(limiteSiguienteMes))
                .collect(Collectors.toList());

            System.out.println("▶️ Proyecto: " + proyecto.getNombre() + " | Retrasadas: " + retrasadas.size() + " | Próximas: " + proximas.size());

            // Filtro 1: Si no hay tareas, se salta el proyecto
            if (retrasadas.isEmpty() && proximas.isEmpty()) {
                System.out.println("   -> Omitido: Todo al día.");
                continue;
            }

            int correosEnviados = 0;

            for (Usuario usuario : todosLosUsuarios) {
                // Filtro 2: Usuario deshabilitado o sin correo
                if (!usuario.isEnabled() || usuario.getCorreo() == null) {
                    continue;
                }

                // Filtro 3: Validación de Rol y Departamento (usamos trim() para quitar espacios accidentales)
                boolean esAdmin = usuario.getRoles().stream()
                        .anyMatch(r -> r.getNombre().toUpperCase().contains("ADMIN"));

                boolean esProjectManager = usuario.getDepartamento() != null && 
                        usuario.getDepartamento().trim().equalsIgnoreCase("PROJECT MANAGER");

                if (esAdmin || esProjectManager) {
                    System.out.println("   📧 Intentando enviar a: " + usuario.getCorreo() + " (Admin: " + esAdmin + " | PM: " + esProjectManager + ")");
                    
                    try {
                        emailServicio.enviarAlertaEstadoProyecto(usuario, proyecto, retrasadas, proximas);
                        correosEnviados++;
                    } catch (Exception e) {
                        System.err.println("   ❌ Error crítico al enviar a " + usuario.getCorreo() + ": " + e.getMessage());
                    }
                }
            }
            System.out.println("   ✅ Correos enviados para este proyecto: " + correosEnviados);
        }
        System.out.println("⏳ [FIN] Escaneo finalizado.\n-----------------------------------");
    }
}


