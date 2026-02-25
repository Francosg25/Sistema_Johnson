package com.johnson.practica.controlador;

import com.johnson.practica.modelo.Adjunto;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.repositorio.AdjuntoRepositorio;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/subir/{itemId}")
    public String subirEvidencia(@PathVariable Long itemId, 
                                 @RequestParam("archivo") MultipartFile archivo,
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
            redirectAttributes.addFlashAttribute("exito", "Archivo subido correctamente.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error interno al guardar el archivo.");
        }

        return "redirect:/proyectos/checklist/" + item.getProyecto().getId();
    }


    @PostMapping("/subir-ajax/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> subirEvidenciaAjax(
            @PathVariable Long itemId, 
            @RequestParam("archivo") MultipartFile archivo) {
        
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


    @PostMapping("/eliminar-ajax/{adjuntoId}")
    @ResponseBody
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
        List<Map<String, Object>> listaSegura = new java.util.ArrayList<>();
        
        for (Adjunto a : adjuntos) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("nombreArchivo", a.getNombreArchivo() != null ? a.getNombreArchivo() : "Documento");
            map.put("subidoEn", a.getSubidoEn());
            map.put("proyectoNombre", a.getProyecto() != null ? a.getProyecto().getNombre() : "Sin Proyecto");
            
            if (a.getElementoChecklist() != null) {
                map.put("entregableInfo", a.getElementoChecklist().getCodigo() + " - " + a.getElementoChecklist().getNombre());
            } else {
                map.put("entregableInfo", "Desconocido");
            }
            
            listaSegura.add(map);
        }
        
        model.addAttribute("adjuntos", listaSegura);
        model.addAttribute("currentUri", "/evidencias");
        
        return "evidencias";
    }
}