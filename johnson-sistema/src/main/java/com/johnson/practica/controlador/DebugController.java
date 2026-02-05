package com.johnson.practica.controlador;

import com.johnson.practica.model.ElementoChecklist;
import com.johnson.practica.model.Proyecto;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.servicio.ProyectoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private ProyectoServicio proyectoServicio;

    @Autowired
    private ElementoChecklistRepositorio elementoRepositorio;

    // Endpoint temporal: crea un proyecto de prueba y 19 elementos, redirige al checklist
    @GetMapping("/crear-proyecto")
    public String crearProyectoPrueba() {
        Proyecto p = new Proyecto();
        p.setNombre("Proyecto Demo");
        p.setNumeroParte("PN-DEMO-001");
        p.setCliente("Cliente Demo");
        p.setFechaSOP(LocalDate.now().plusMonths(3));
        p.setEstado("ON_TRACK");
        p.setCreadoEn(LocalDateTime.now());

        Proyecto guardado = proyectoServicio.guardarProyecto(p);

        // Si no se generaron elementos (catalogo vacío), crear manualmente 19 elementos
        List<ElementoChecklist> existentes = elementoRepositorio.findByProyectoId(guardado.getId());
        if (existentes == null || existentes.isEmpty()) {
            List<String> nombresPPAP = List.of(
                    "1. Registros de Diseño", "2. Cambios de Ingeniería", "3. Aprobación Ingeniería Cliente",
                    "4. DFMEA", "5. Diagrama de Flujo", "6. PFMEA", "7. Plan de Control",
                    "8. MSA", "9. Dimensionales", "10. Material / Desempeño", "11. Cpk/Ppk",
                    "12. Laboratorio Calificado", "13. AAR", "14. Muestras Producto", "15. Muestra Maestra",
                    "16. Ayudas Verificación", "17. Requisitos Cliente", "18. PSW", "19. Checklist PPAP"
            );

            List<ElementoChecklist> nuevos = new ArrayList<>();
            for (String nombre : nombresPPAP) {
                ElementoChecklist e = ElementoChecklist.builder()
                        .proyecto(guardado)
                        .titulo(nombre)
                        .descripcion("")
                        .fase("4. PPAP")
                        .estado("ABIERTO")
                        .build();
                nuevos.add(e);
            }
            elementoRepositorio.saveAll(nuevos);
        }

        return "redirect:/proyectos/" + guardado.getId() + "/checklist";
    }
}
