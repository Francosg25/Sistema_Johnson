package com.johnson.practica.servicio;

import com.johnson.practica.model.*;
import com.johnson.practica.repositorio.*; // Importante para ver los repositorios
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProyectoServicio { // Nombre de clase en Español

    @Autowired
    private ProyectoRepositorio proyectoRepositorio; // En Español

    @Autowired
    private CatalogoElementoRepositorio catalogoRepositorio; // En Español

    @Autowired
    private ElementoChecklistRepositorio checklistRepositorio; // En Español

    @Transactional
    public Proyecto guardarProyecto(Proyecto proyecto) {
        if (proyecto.getId() == null) {
            // Aseguramos una fecha si no viene
            proyecto.setFechaSOP(LocalDate.now().plusMonths(6)); 
        }
        Proyecto proyectoGuardado = proyectoRepositorio.save(proyecto);

        if (checklistRepositorio.findByProyectoId(proyectoGuardado.getId()).isEmpty()) {
            generarChecklistInicial(proyectoGuardado);
        }
        return proyectoGuardado;
    }

    // Método necesario para el Controlador
    public Proyecto buscarPorId(Long id) {
        return proyectoRepositorio.findById(id).orElse(null);
    }

    private void generarChecklistInicial(Proyecto proyecto) {
        List<CatalogoElemento> catalogo = catalogoRepositorio.findAll();
        List<ElementoChecklist> nuevosElementos = new ArrayList<>();

        for (CatalogoElemento itemBase : catalogo) {
            ElementoChecklist elemento = new ElementoChecklist();
            elemento.setProyecto(proyecto);
            elemento.setTitulo(itemBase.getNombre());
            elemento.setFase(itemBase.getFase());
            elemento.setEstado("ABIERTO");
            
            nuevosElementos.add(elemento);
        }
        checklistRepositorio.saveAll(nuevosElementos);
    }

    public List<Proyecto> obtenerTodos() {
        return proyectoRepositorio.findAll();
    }
}