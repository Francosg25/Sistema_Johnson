package com.johnson.practica.controlador;

import com.johnson.practica.modelo.Adjunto;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.repositorio.AdjuntoRepositorio;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/evidencias")
public class EvidenciaControlador {

    @Autowired
    private ElementoChecklistRepositorio elementoRepositorio;

    @Autowired
    private AdjuntoRepositorio adjuntoRepositorio;

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/subir/{itemId}")
    public String subirEvidencia(@PathVariable Long itemId, 
                                 @RequestParam("archivo") MultipartFile archivo,
                                 RedirectAttributes redirectAttributes) {
        
        ElementoChecklist item = elementoRepositorio.findById(itemId).orElse(null);
        if (item == null) {
            return "redirect:/"; // Si no existe, regresamos al inicio
        }

        Long proyectoId = item.getProyecto().getId();

        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor selecciona un archivo válido.");
            return "redirect:/proyectos/checklist/" + proyectoId;
        }

        try {
            // Crear la carpeta 'uploads' si es la primera vez que subimos algo
            File directorio = new File(UPLOAD_DIR);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            String nombreArchivoReal = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path rutaDestino = Paths.get(UPLOAD_DIR + nombreArchivoReal);
            Files.write(rutaDestino, archivo.getBytes());

            Adjunto adjunto = new Adjunto();
            adjunto.setNombreArchivo(archivo.getOriginalFilename());
            adjunto.setTipoContenido(archivo.getContentType());
            adjunto.setRuta(rutaDestino.toString());
            adjunto.setElementoChecklist(item);
            adjunto.setProyecto(item.getProyecto());
            adjunto.setSubidoEn(LocalDateTime.now());
            
            adjuntoRepositorio.save(adjunto);

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error interno al guardar el archivo.");
        }

        return "redirect:/proyectos/checklist/" + proyectoId;
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
            File directorio = new File(UPLOAD_DIR);
            if (!directorio.exists()) directorio.mkdirs();

            String nombreArchivoReal = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path rutaDestino = Paths.get(UPLOAD_DIR + nombreArchivoReal);
            Files.write(rutaDestino, archivo.getBytes());

            Adjunto adjunto = new Adjunto();
            adjunto.setNombreArchivo(archivo.getOriginalFilename());
            adjunto.setTipoContenido(archivo.getContentType());
            adjunto.setRuta(rutaDestino.toString());
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
        try {
            Adjunto adjunto = adjuntoRepositorio.findById(adjuntoId).orElse(null);
            if (adjunto == null) return ResponseEntity.notFound().build();

            Path rutaArchivo = Paths.get(adjunto.getRuta());
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            if (recurso.exists() && recurso.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(adjunto.getTipoContenido()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + adjunto.getNombreArchivo() + "\"")
                        .body(recurso);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/eliminar-ajax/{adjuntoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarEvidenciaAjax(@PathVariable Long adjuntoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Adjunto adjunto = adjuntoRepositorio.findById(adjuntoId).orElse(null);
            if (adjunto != null) {
                // 1. Borrar el archivo físico del disco duro
                Path rutaArchivo = Paths.get(adjunto.getRuta());
                Files.deleteIfExists(rutaArchivo);
                
                // 2. Borrar el registro de PostgreSQL
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




}