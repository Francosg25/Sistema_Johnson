package com.johnson.practica.controlador;

import com.johnson.practica.modelo.HitoProyecto;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.HitoProyectoRepositorio;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.servicio.NotificacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hitos")
public class MilestoneControlador {

    @Autowired
    private HitoProyectoRepositorio hitoRepo;

    @Autowired
    private ProyectoRepositorio proyectoRepo;

    @Autowired
    private com.johnson.practica.repositorio.ElementoChecklistRepositorio elementoRepo;

    @Autowired
    private NotificacionServicio notificacionServicio;

    @GetMapping("/proyecto/{id}")
    public List<HitoProyecto> obtenerHitosPorProyecto(@PathVariable Long id) {
        return hitoRepo.findByProyecto_Id(id);
    }

    @GetMapping("/proyecto/{id}/elementos")
    public ResponseEntity<List<Map<String, Object>>> obtenerElementosProyecto(@PathVariable Long id) {
        List<com.johnson.practica.modelo.ElementoChecklist> elementos = elementoRepo.findByProyecto_Id(id);
        
        List<Map<String, Object>> respuestaLimpia = new ArrayList<>();
        for (com.johnson.practica.modelo.ElementoChecklist el : elementos) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", el.getId());
            map.put("codigo", el.getCodigo());
            map.put("nombre", el.getNombre());
            map.put("fase", el.getFase());
            map.put("grupo", el.getGrupo());
            map.put("esMainEvent", el.isEsMainEvent());
            respuestaLimpia.add(map);
        }
        
        return ResponseEntity.ok(respuestaLimpia);
    }

    @PostMapping("/guardar")
    public ResponseEntity<?> guardarHito(@RequestBody Map<String, String> payload, Principal principal) {
        Long proyectoId = Long.parseLong(payload.get("proyectoId"));
        String nombre = payload.get("nombre");
        LocalDate fecha = LocalDate.parse(payload.get("fecha"));
        String etapa = payload.get("etapa");
        Integer porcentaje = payload.containsKey("porcentaje") ? Integer.parseInt(payload.get("porcentaje")) : 100;

        Proyecto p = proyectoRepo.findById(proyectoId).orElseThrow();
        
        HitoProyecto hito = new HitoProyecto(nombre, fecha, etapa, p, porcentaje);
        
        boolean esNuevo = true;
        if (payload.containsKey("id") && !payload.get("id").isEmpty()) {
            hito.setId(Long.parseLong(payload.get("id")));
            esNuevo = false;
        }
        
        hitoRepo.save(hito);

        if (esNuevo) {
            String autor = (principal != null) ? principal.getName() : "Sistema";
            notificacionServicio.alertarATodos(
                "Nuevo Hito: " + nombre,
                "Se ha creado un nuevo hito '" + nombre + "' en el proyecto " + p.getNombre(),
                "INFO",
                "/timeline",
                autor
            );
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/toggle-main-event/{id}")
    public ResponseEntity<?> toggleMainEvent(@PathVariable Long id) {
        com.johnson.practica.modelo.ElementoChecklist el = elementoRepo.findById(id).orElseThrow();
        el.setEsMainEvent(!el.isEsMainEvent());
        elementoRepo.save(el);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarHito(@PathVariable Long id) {
        hitoRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}