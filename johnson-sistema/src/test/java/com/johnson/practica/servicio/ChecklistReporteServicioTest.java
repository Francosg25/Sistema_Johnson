package com.johnson.practica.servicio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChecklistReporteServicioTest {

    @Mock
    private ProyectoRepositorio proyectoRepositorio;

    @Mock
    private ElementoChecklistRepositorio elementoRepositorio;

    @InjectMocks
    private ChecklistReporteServicio reporteServicio;

    private Proyecto proyectoActivo;
    private ElementoChecklist tareaRetrasada;

    @BeforeEach
    void setUp() {
        proyectoActivo = new Proyecto();
        proyectoActivo.setId(1L);
        proyectoActivo.setNombre("Sistema_Johnson Test");
        proyectoActivo.setEsHistorico(false);

        tareaRetrasada = new ElementoChecklist();
        tareaRetrasada.setId(100L);
        tareaRetrasada.setEsMainEvent(true);
        tareaRetrasada.setFechaPlan(LocalDate.now().minusDays(5)); // Venció hace 5 días
        tareaRetrasada.setScore("PENDIENTE");
        tareaRetrasada.setProyecto(proyectoActivo);
    }

    @Test
    void debeDetectarEntregableRetrasadoYAsignarColorRojo() {
        // Configurar el comportamiento falso (Mock) de la base de datos
        when(proyectoRepositorio.findAllByOrderByIdAsc()).thenReturn(List.of(proyectoActivo));
        when(elementoRepositorio.findByProyecto_Id(1L)).thenReturn(List.of(tareaRetrasada));

        // Ejecutar el método real
        Map<String, Object> resultado = reporteServicio.obtenerDatosTimeline();

        // Verificar resultados
        assertNotNull(resultado);
        List<?> items = (List<?>) resultado.get("items");
        assertFalse(items.isEmpty(), "La lista de items no debería estar vacía");
        
        // En tu lógica, este elemento debería tener la clase "event-delayed"
        assertTrue(items.get(0).toString().contains("event-delayed"), 
            "El entregable vencido y sin OK debe ser marcado con event-delayed (Rojo)");
    }
}