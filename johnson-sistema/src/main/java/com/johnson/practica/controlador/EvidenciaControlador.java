package com.johnson.practica.controlador;

import com.johnson.practica.modelo.Adjunto;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.repositorio.AdjuntoRepositorio;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/evidencias")
public class EvidenciaControlador {

    @Autowired
    private ElementoChecklistRepositorio elementoRepositorio;

    @Autowired
    private AdjuntoRepositorio adjuntoRepositorio;

    @Autowired
    private com.johnson.practica.servicio.NotificacionServicio notificacionServicio;


    @PostMapping("/subir/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    public String subirEvidencia(@PathVariable Long itemId, 
                                 @RequestParam("archivo") MultipartFile archivo,
                                 java.security.Principal principal,
                                 RedirectAttributes redirectAttributes) {
        
        ElementoChecklist item = elementoRepositorio.findById(itemId).orElse(null);
        if (item == null) return "redirect:/"; 

        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor selecciona un archivo válido.");
            return "redirect:/proyectos/checklist/" + item.getProyecto().getId();
        }

        try {
            Adjunto adjunto = new Adjunto();
            adjunto.setNombreArchivo(archivo.getOriginalFilename());
            adjunto.setTipoContenido(archivo.getContentType());
            
            adjunto.setDatos(archivo.getBytes()); 
            
            adjunto.setElementoChecklist(item);
            adjunto.setProyecto(item.getProyecto());
            adjunto.setSubidoEn(LocalDateTime.now());
            
            adjuntoRepositorio.save(adjunto);
            
            String autor = (principal != null) ? principal.getName() : "Sistema";
            notificacionServicio.alertarATodos(
                "New Evidence Uploaded",
                autor + " uploaded evidence for '" + item.getNombre() + "' in " + item.getProyecto().getNombre(),
                "INFO",
                "/proyectos/checklist/" + item.getProyecto().getId(),
                autor
            );
  
            redirectAttributes.addFlashAttribute("exito", "Archivo subido correctamente.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error interno al guardar el archivo.");
        }

        return "redirect:/proyectos/checklist/" + item.getProyecto().getId();
    }


    @PostMapping("/subir-ajax/{itemId}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    public ResponseEntity<Map<String, Object>> subirEvidenciaAjax(
            @PathVariable Long itemId, 
            @RequestParam("archivo") MultipartFile archivo,
            java.security.Principal principal) {
        
        Map<String, Object> response = new HashMap<>();
        
        ElementoChecklist item = elementoRepositorio.findById(itemId).orElse(null);
        if (item == null || archivo.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Error: Archivo o entregable no válido");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Adjunto adjunto = new Adjunto();
            adjunto.setNombreArchivo(archivo.getOriginalFilename());
            adjunto.setTipoContenido(archivo.getContentType());
            
            adjunto.setDatos(archivo.getBytes()); 
            
            adjunto.setElementoChecklist(item);
            adjunto.setProyecto(item.getProyecto());
            adjunto.setSubidoEn(LocalDateTime.now());
            
            adjuntoRepositorio.save(adjunto);

            String autor = (principal != null) ? principal.getName() : "Sistema";
            notificacionServicio.alertarATodos(
                "New Evidence Uploaded",
                autor + " uploaded evidence for '" + item.getNombre() + "' in " + item.getProyecto().getNombre(),
                "INFO",
                "/proyectos/checklist/" + item.getProyecto().getId(),
                autor
            );

            response.put("exito", true);
            response.put("mensaje", "Archivo guardado con éxito");
            response.put("adjuntoId", adjunto.getId()); 
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("exito", false);
            response.put("mensaje", "Error interno al guardar");
            return ResponseEntity.status(500).body(response);
        }
    }

   
    @GetMapping("/descargar/{adjuntoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    public ResponseEntity<Resource> descargarEvidencia(@PathVariable Long adjuntoId) {
        Adjunto adjunto = adjuntoRepositorio.findById(adjuntoId).orElse(null);
        
        if (adjunto == null || adjunto.getDatos() == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource recurso = new ByteArrayResource(adjunto.getDatos());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(adjunto.getTipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + adjunto.getNombreArchivo() + "\"")
                .body(recurso);
    }

    @GetMapping("/visualizar/{adjuntoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    public ResponseEntity<Resource> visualizarEvidencia(@PathVariable Long adjuntoId) {
        Adjunto adjunto = adjuntoRepositorio.findById(adjuntoId).orElse(null);
        
        if (adjunto == null || adjunto.getDatos() == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource recurso = new ByteArrayResource(adjunto.getDatos());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(adjunto.getTipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + adjunto.getNombreArchivo() + "\"")
                .body(recurso);
    }
    

    @PostMapping("/eliminar-ajax/{adjuntoId}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    public ResponseEntity<Map<String, Object>> eliminarEvidenciaAjax(@PathVariable Long adjuntoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Adjunto adjunto = adjuntoRepositorio.findById(adjuntoId).orElse(null);
            if (adjunto != null) {
                adjuntoRepositorio.delete(adjunto); 
                response.put("exito", true);
                return ResponseEntity.ok(response);
            }
            response.put("exito", false);
            response.put("mensaje", "Archivo no encontrado");
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al eliminar el archivo");
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    public String verRepositorioGlobal(Model model) {
        List<Adjunto> adjuntos = adjuntoRepositorio.findAllConDetalles(); 
        
        Map<String, List<Map<String, Object>>> activos = new java.util.LinkedHashMap<>();
        
        for (Adjunto a : adjuntos) {
            boolean isArchivado = (a.getProyecto() != null) && a.getProyecto().isArchivado();
            if (isArchivado) continue; // Ignorar archivados en esta vista

            String proyectoNombre = (a.getProyecto() != null) ? a.getProyecto().getNombre() : "Otros / Sin Proyecto";
            
            Map<String, Object> infoAdjunto = new HashMap<>();
            infoAdjunto.put("id", a.getId());
            infoAdjunto.put("nombreArchivo", a.getNombreArchivo() != null ? a.getNombreArchivo() : "Documento");
            infoAdjunto.put("subidoEn", a.getSubidoEn());
            infoAdjunto.put("entregableInfo", (a.getElementoChecklist() != null) ? 
                a.getElementoChecklist().getNombre() : "Desconocido");
            infoAdjunto.put("proyectoId", (a.getProyecto() != null) ? a.getProyecto().getId() : null);
            
            activos.computeIfAbsent(proyectoNombre, k -> new java.util.ArrayList<>()).add(infoAdjunto);
        }
        
        model.addAttribute("proyectosConEvidencias", activos);
        model.addAttribute("currentUri", "/evidencias");
        
        return "proyectos/evidencias";
    }

    @GetMapping("/proyecto/{proyectoId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerEvidenciasPorProyecto(@PathVariable Long proyectoId) {
        List<Adjunto> adjuntos = adjuntoRepositorio.findByProyecto_Id(proyectoId);
        List<Map<String, Object>> response = new java.util.ArrayList<>();
        
        for (Adjunto a : adjuntos) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("nombre", a.getNombreArchivo());
            map.put("fecha", a.getSubidoEn());
            map.put("entregable", (a.getElementoChecklist() != null) ? a.getElementoChecklist().getNombre() : "N/A");
            response.add(map);
        }
        return response;
    }







}