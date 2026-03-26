package com.johnson.practica.servicio;

import com.johnson.practica.dto.TimelineItem;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.repositorio.HitoProyectoRepositorio;
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
import static org.mockito.ArgumentMatchers.anyLong;
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

    @Mock
    private HitoProyectoRepositorio hitoProyectoRepositorio; 


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
        // 1. Aseguramos que los mocks NO devuelvan null, sino listas vacías
        when(proyectoRepositorio.findAllByOrderByIdAsc()).thenReturn(List.of(proyectoActivo));
        when(elementoRepositorio.findByProyecto_Id(anyLong())).thenReturn(List.of(tareaRetrasada));
        when(hitoProyectoRepositorio.findByProyecto_Id(anyLong())).thenReturn(List.of());

        // 2. Ejecutar el servicio
        Map<String, Object> resultado = reporteServicio.obtenerDatosTimeline();

        // 3. Verificamos qué llaves tiene el mapa para no fallar por un nombre
        // Probamos con "timelineItems" o "items"
        List<TimelineItem> items = (List<TimelineItem>) resultado.get("timelineItems");
        if (items == null) {
            items = (List<TimelineItem>) resultado.get("items");
        }

        // 4. Ahora sí, las validaciones
        assertNotNull(items, "El mapa debe contener una lista de items (revisa si la llave es 'items' o 'timelineItems')");
        assertFalse(items.isEmpty(), "La lista de items no debería estar vacía");
        
        String claseObtenida = items.get(0).getClassName();
        assertTrue(claseObtenida.contains("event-delayed"), 
            "Se esperaba event-delayed pero se obtuvo: " + claseObtenida);
    }
}